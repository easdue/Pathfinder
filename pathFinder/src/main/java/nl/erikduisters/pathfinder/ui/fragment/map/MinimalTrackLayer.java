package nl.erikduisters.pathfinder.ui.fragment.map;

import android.content.Context;
import android.support.annotation.Nullable;

import org.oscim.backend.CanvasAdapter;
import org.oscim.backend.canvas.Bitmap;
import org.oscim.core.GeoPoint;
import org.oscim.layers.marker.ItemizedLayer;
import org.oscim.layers.marker.MarkerItem;
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
public class MinimalTrackLayer extends ItemizedLayer<MarkerItem> {
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

        List<MarkerItem> markerItems = new ArrayList<>(minimalTrackList.getMinimalTracks().size());

        for (MinimalTrack minimalTrack : minimalTrackList.getMinimalTracks()) {
            GeoPoint geoPoint = new GeoPoint(minimalTrack.startLatitude, minimalTrack.startLongitude);
            MarkerItem markerItem = new MarkerItem(minimalTrack.name, "", geoPoint);
            markerItem.setMarker(markerSymbol);

            markerItems.add(markerItem);
        }

        addItems(markerItems);
    }
}
