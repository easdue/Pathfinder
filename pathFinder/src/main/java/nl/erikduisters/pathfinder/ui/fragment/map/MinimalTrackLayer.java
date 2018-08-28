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

package nl.erikduisters.pathfinder.ui.fragment.map;

import android.content.Context;
import android.support.annotation.Nullable;

import org.oscim.backend.CanvasAdapter;
import org.oscim.backend.canvas.Bitmap;
import org.oscim.layers.marker.ItemizedLayer;
import org.oscim.layers.marker.MarkerSymbol;
import org.oscim.map.Map;
import org.oscim.utils.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import nl.erikduisters.pathfinder.R;
import nl.erikduisters.pathfinder.data.model.MinimalTrack;
import nl.erikduisters.pathfinder.data.model.MinimalTrackList;

/**
 * Created by Erik Duisters on 25-08-2018.
 */
//TODO: Item listener
//TODO: Zoom dependent symbol size
//TODO: Select track on marker click
//TODO: Different MarkerSymbol for selected track
public class MinimalTrackLayer extends ItemizedLayer<MinimalTrackMarker> {
    private MarkerSymbol markerSymbol;

    public MinimalTrackLayer(Map map, Context context) {
        super(map, (MarkerSymbol)null);

        InputStream inputStream = null;

        try {
            inputStream = context.getResources().openRawResource(R.raw.ic_map_panel1);

            int width = context.getResources().getDimensionPixelSize(R.dimen.map_track_marker_width);
            int height = context.getResources().getDimensionPixelSize(R.dimen.map_track_marker_height);

            //TODO: All bitmaps used on the map should be in a TextureAtlas
            Bitmap trackMarker = CanvasAdapter.decodeSvgBitmap(inputStream, width, height, 100);
            markerSymbol = new MarkerSymbol(trackMarker, MarkerSymbol.HotspotPlace.BOTTOM_CENTER);
        } catch (IOException e) {
            IOUtils.closeQuietly(inputStream);
            throw new RuntimeException(e.getMessage());
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    void setMinimalTrackList(@Nullable MinimalTrackList minimalTrackList) {
        removeAllItems(false);

        if (minimalTrackList == null) {
            populate();
            return;
        }

        List<MinimalTrackMarker> markerItems = new ArrayList<>(minimalTrackList.getMinimalTracks().size());

        for (MinimalTrack minimalTrack : minimalTrackList.getMinimalTracks()) {
            MinimalTrackMarker marker = new MinimalTrackMarker(minimalTrack, markerSymbol);

            markerItems.add(marker);
        }

        addItems(markerItems);
    }
}
