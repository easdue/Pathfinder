/*
 * Copyright (c) 2018 Erik Duisters
 *
 * This file is part of Pathfinder
 *
 * Pathfinder is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Foobar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Pathfinder. If not, see <https://www.gnu.org/licenses/>.
 */

package nl.erikduisters.pathfinder.data.usecase;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.support.annotation.NonNull;

import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import nl.erikduisters.pathfinder.async.Cancellable;
import nl.erikduisters.pathfinder.ui.widget.SvgView;
import nl.erikduisters.pathfinder.util.FileUtil;
import timber.log.Timber;

/**
 * Created by Erik Duisters on 13-07-2018.
 */

public class RenderSvgView extends UseCase<RenderSvgView.RequestInfo, SvgView> {
    private StringBuilder fileNameTemplate;
    private int versionStart;
    private HashMap<String, Bitmap> bitmapMap;

    public RenderSvgView(RequestInfo requestInfo, @NonNull Callback<SvgView> callback) {
        super(requestInfo, callback);

        fileNameTemplate = new StringBuilder();
    }

    @Override
    public void execute(Cancellable cancellable) {
        String resourceName;
        Bitmap bitmap;
        boolean fault = false;

        SvgView svgView = requestInfo.svgView;

        try {
            resourceName = svgView.getResources().getResourceEntryName(svgView.getSvgResourceId());
        } catch (Resources.NotFoundException e) {
            callback.onError(e);
            return;
        }

        Timber.d("execute() - rendering: %s layers: %s width: %d height: %d", resourceName, svgView.getLayerList().toString(), svgView.getSvgWidth(), svgView.getSvgHeight());

        List<String> layerList = svgView.getLayerList();
        int layerListSize = layerList.size();
        bitmapMap = new HashMap<>(requestInfo.svgView.getLayerList().size());

        for (int i = 0; i < layerListSize && !fault; i++) {
            String layer = layerList.get(i);

            createFileNameTemplate(resourceName, layer);

            bitmap = loadBitmapFromCache(cancellable);

            if (!cancellable.isCancelled() && bitmap == null) {
                bitmap = renderBitmapFromSvg(layer, cancellable);

                if (bitmap == null) {
                    fault = true;
                }
            }

            if (fault) {
                clearBitmapMap();
            } else {
                if (!cancellable.isCancelled()) {
                    saveBitmap(bitmap);
                }

                bitmapMap.put(layer, bitmap);
            }
        }

        if (!fault) {
            svgView.setBitmapLayers(bitmapMap);
            callback.onResult(svgView);
        }
    }

    private void saveBitmap(Bitmap bitmap) {
        BufferedOutputStream bos = null;

        fileNameTemplate.append(requestInfo.version)
                .append(".png");

        File outFile = new File(requestInfo.svgCacheDir, fileNameTemplate.toString());

        try {
            bos = new BufferedOutputStream(new FileOutputStream(outFile.getAbsolutePath(), false));
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
            bos.flush();
        } catch (FileNotFoundException e) {
            Timber.d("saveBitmap caught an FileNotFoundException: %s", e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            callback.onError(e);
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    // To bad
                }
            }
        }
    }

    private void createFileNameTemplate(String baseName, String layer) {
        if (fileNameTemplate.length() != 0) {
            fileNameTemplate.setLength(0);
        }

        fileNameTemplate.append(baseName)
                .append('-')
                .append(requestInfo.svgView.getSvgWidth())
                .append('x')
                .append(requestInfo.svgView.getSvgHeight());

        if (layer != null) {
            fileNameTemplate.append('_')
                    .append(layer);
        }

        fileNameTemplate.append("_v");
        versionStart = fileNameTemplate.length();
    }

    private Bitmap loadBitmapFromCache(Cancellable cancellable) {
        List<File> files = FileUtil.getFilesByFilter(requestInfo.svgCacheDir, new FileUtil.RegexFileFilter(fileNameTemplate + "[0-9]+\\.png"));

        for (File f : files) {
            if (cancellable.isCancelled()) {
                return null;
            }

            String name = f.getName();

            int versionStop = name.indexOf(".png");

            if (Integer.valueOf(name.substring(versionStart, versionStop)) == requestInfo.version) {
                BufferedInputStream is = null;

                try {
                    is = new BufferedInputStream(new FileInputStream(f));

                    return BitmapFactory.decodeStream(is);
                } catch (FileNotFoundException e) {
                    Timber.e("loadBitmapFromCache caught a FileNotFoundException: %s", e.getMessage());
                    return null;
                } finally {
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException e) {
                            //To bad
                        }
                    }
                }
            } else {
                f.delete();
            }
        }
        return null;
    }

    private void clearBitmapMap() {
        Iterator<Map.Entry<String, Bitmap>> it = bitmapMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Bitmap> entry = it.next();
            entry.getValue().recycle();
            it.remove();
        }
    }

    private Bitmap renderBitmapFromSvg(String layer, Cancellable cancellable) {
        SVG svg;

        try {
            svg = SVG.getFromResource(requestInfo.svgView.getResources(), requestInfo.svgView.getSvgResourceId());
        } catch (SVGParseException e) {
            callback.onError(e);
            return null;
        } catch (Resources.NotFoundException e) {
            //Already handled in render()
            return null;
        }

        if (cancellable.isCancelled()) {
            return null;
        }

        Bitmap bitmap = null;

        try {
            bitmap = Bitmap.createBitmap(requestInfo.svgView.getSvgWidth(), requestInfo.svgView.getSvgHeight(), Bitmap.Config.ARGB_8888);
            bitmap.setDensity(requestInfo.svgView.getSvgDensity());
            Canvas canvas = new Canvas(bitmap);
            svg.renderToCanvas(canvas, null, layer);
        } catch (IllegalArgumentException e) {
            Timber.d("renderBitmapFromSvg caught an IllegalArgumentException: %s", e.getMessage());
        }

        if (cancellable.isCancelled() && bitmap != null) {
            bitmap = null;
        }

        return bitmap;
    }

    public static class RequestInfo {
        public final SvgView svgView;
        final String svgCacheDir;
        final int version;

        public RequestInfo(SvgView svgView, String svgCacheDir, int version) {
            this.svgView = svgView;
            this.svgCacheDir = svgCacheDir;
            this.version = version;
        }
    }
}
