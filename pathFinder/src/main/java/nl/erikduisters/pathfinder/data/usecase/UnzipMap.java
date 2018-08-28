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

import android.content.ContentResolver;
import android.net.Uri;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import org.oscim.utils.IOUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import nl.erikduisters.pathfinder.async.Cancellable;
import nl.erikduisters.pathfinder.data.usecase.UnzipMap.Result.UnzipFailed.Reason;
import timber.log.Timber;

/**
 * Created by Erik Duisters on 30-07-2018.
 */

public class UnzipMap extends ProgressUseCase<UnzipMap.RequestInfo, UnzipMap.Progress, UnzipMap.Result> {
    private final int BUFFER_SIZE = 4096;
    private final byte[] buffer = new byte[BUFFER_SIZE];

    public UnzipMap(@NonNull RequestInfo requestInfo, @NonNull Callback<Progress, Result> callback) {
        super(requestInfo, callback);
    }

    @Override
    public void execute(Cancellable cancellable) {
        InputStream inputStream = null;

        try {
            String scheme = requestInfo.zippedMapFileUri.getScheme();

            if (scheme.equals("file")) {
                File zippedMapFile = new File(requestInfo.zippedMapFileUri.getPath());

                if (!zippedMapFile.isFile() || !zippedMapFile.canRead()) {
                    getCallback().onResult(new Result.UnzipFailed(requestInfo.mapDownloadId, requestInfo.zipFileName, Reason.FILE_NOT_FOUND));
                } else {
                    unzipFile(zippedMapFile, cancellable);
                }
            } else if (scheme.equals("content")) {
                try {
                    inputStream = requestInfo.contentResolver.openInputStream(requestInfo.zippedMapFileUri);

                    unzipStream(inputStream, cancellable);
                } catch (FileNotFoundException e) {
                    getCallback().onResult(new Result.UnzipFailed(requestInfo.mapDownloadId, requestInfo.zipFileName, Reason.FILE_NOT_FOUND));
                }
            } else {
                throw new IllegalArgumentException("Don't know how to handle scheme: " + scheme);
            }
        } catch (Exception e) {
            Timber.e("UnzipMap.execute threw: %s", e.toString());

            if (e instanceof ZipException) {
                getCallback().onResult(new Result.UnzipFailed(requestInfo.mapDownloadId, requestInfo.zipFileName, Reason.ZIPFILE_CORRUPTED));
            } else {
                getCallback().onError(e);
            }
        } finally {
            IOUtils.closeQuietly(inputStream);
        }

    }

    private boolean isMapEntry(ZipEntry zipEntry) {
        return !zipEntry.isDirectory() && zipEntry.getName().endsWith(".map");
    }

    private String getMapName(ZipEntry zipEntry) {
        String mapName = zipEntry.getName();

        if (mapName.contains(File.separator)) {
            mapName = mapName.substring(mapName.lastIndexOf(File.separatorChar) + 1);
        }

        return mapName;
    }

    private void unzipFile(File file, Cancellable cancellable) throws IOException {
        ZipFile zipFile = null;
        BufferedInputStream inputStream = null;

        try {
            zipFile = new ZipFile(file);

            Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();

            while (zipEntries.hasMoreElements() && !cancellable.isCancelled()) {
                ZipEntry zipEntry = zipEntries.nextElement();

                if (!isMapEntry(zipEntry)) {
                    continue;
                }

                String mapName = getMapName(zipEntry);

                if (!isEnoughSpaceAvailable(zipEntry)) {
                    return;
                }

                inputStream = new BufferedInputStream((zipFile.getInputStream(zipEntry)));

                writeMapToDisk(mapName, inputStream, zipEntry.getSize(), cancellable);

                getCallback().onResult(new Result.UnzipFinished(requestInfo.mapDownloadId, mapName));

                break;
            }
        } finally {
            IOUtils.closeQuietly(inputStream);
            if (zipFile != null) {
                try {
                    zipFile.close();
                } catch (IOException e) {
                    //To bad
                }
            }
        }
    }

    private void unzipStream(InputStream inputStream, Cancellable cancellable) throws IOException {
        BufferedInputStream bufferedInputStream;
        ZipInputStream zipInputStream = null;

        try {
            bufferedInputStream = new BufferedInputStream(inputStream);
            zipInputStream = new ZipInputStream(bufferedInputStream);

            ZipEntry zipEntry;

            while ((zipEntry = zipInputStream.getNextEntry()) != null && !cancellable.isCancelled()) {
                if (!isMapEntry(zipEntry)) {
                    continue;
                }

                String mapName = getMapName(zipEntry);

                if (!isEnoughSpaceAvailable(zipEntry)) {
                    return;
                }

                writeMapToDisk(mapName, zipInputStream, zipEntry.getSize(), cancellable);

                getCallback().onResult(new Result.UnzipFinished(requestInfo.mapDownloadId, mapName));

                break;
            }
        } finally {
            IOUtils.closeQuietly(zipInputStream);
        }

    }

    private boolean isEnoughSpaceAvailable(ZipEntry zipEntry) {
        long size = zipEntry.getSize();

        if (size != -1) {
            if (requestInfo.mapDir.getUsableSpace() >= size) {
                return true;
            } else {
                getCallback().onResult(new Result.UnzipFailed(requestInfo.mapDownloadId, requestInfo.zipFileName, Reason.NOT_ENOUGH_SPACE_AVAILABLE));
                return false;
            }
        }

        return true;
    }

    private void writeMapToDisk(String mapName, InputStream inputStream, double mapSize, Cancellable cancellable) throws IOException {
        boolean deleteMapFile = false;
        int lastReportedProgress = -1;

        if (mapSize == -1) {
            getCallback().onProgress(new Progress(mapName, -1));
        }

        BufferedOutputStream outputStream = null;
        File mapFile = new File(requestInfo.mapDir, mapName);

        long written = 0;

        try {
            outputStream = new BufferedOutputStream(new FileOutputStream(mapFile));

            Timber.d("Start unzipping: %s", mapFile.getName());
            int numRead;
            while ((numRead = inputStream.read(buffer, 0, BUFFER_SIZE)) != -1 && !cancellable.isCancelled()) {
                outputStream.write(buffer, 0, numRead);
                written += numRead;

                if (mapSize != -1) {
                    int progress = (int) Math.round(written / (mapSize / 100));

                    if (progress > lastReportedProgress) {
                        getCallback().onProgress(new Progress(mapName, progress));
                        lastReportedProgress = progress;
                    }
                }
            }

            if (numRead == -1) {
                Timber.d("Unzipping of %s finished", mapFile.getName());
                deleteMapFile = false;
            } else {
                Timber.d("Unzipping got cancelled");
                deleteMapFile = true;
            }
        } catch (IOException e) {
            deleteMapFile = true;

            throw e;
        } finally {
            IOUtils.closeQuietly(outputStream);

            if (deleteMapFile) {
                mapFile.delete();
            }
        }
    }

    public static class RequestInfo {
        public final long mapDownloadId;
        public final String zipFileName;
        final File mapDir;
        final Uri zippedMapFileUri;
        final ContentResolver contentResolver;


        public RequestInfo(long mapDownloadId, String zipFileName, File mapDir, Uri zippedMapFileUri, ContentResolver contentResolver) {
            this.mapDownloadId = mapDownloadId;
            this.zipFileName = zipFileName;
            this.mapDir = mapDir;
            this.zippedMapFileUri = zippedMapFileUri;
            this.contentResolver = contentResolver;
        }
    }

    public interface Result {
        public class UnzipFinished implements Result {
            public final long mapDownloadId;
            public final String mapName;

            public UnzipFinished(long mapDownloadId, String mapName) {
                this.mapDownloadId = mapDownloadId;
                this.mapName = mapName;
            }
        }

        public class UnzipFailed implements Result {
            @IntDef({Reason.FILE_NOT_FOUND, Reason.NOT_ENOUGH_SPACE_AVAILABLE, Reason.ZIPFILE_CORRUPTED})
            @Retention(RetentionPolicy.SOURCE)
            public @interface Reason {
                int FILE_NOT_FOUND = 0;
                int ZIPFILE_CORRUPTED = 1;
                int NOT_ENOUGH_SPACE_AVAILABLE = 2;
            }
            public final long mapDownloadId;
            public final String zipFileName;
            public final @Reason int reason;

            public UnzipFailed(long mapDownloadId, String zipFileName, @Reason int reason) {
                this.mapDownloadId = mapDownloadId;
                this.zipFileName = zipFileName;
                this.reason = reason;
            }
        }
    }

    public static class Progress {
        public final String mapName;
        /**
         * Progress of the unzip operation, range 0 - 100 or -1 for unknown;
         */
        public final int progress;

        public Progress(String mapName, int progress) {
            this.mapName = mapName;
            this.progress = progress;
        }
    }
}
