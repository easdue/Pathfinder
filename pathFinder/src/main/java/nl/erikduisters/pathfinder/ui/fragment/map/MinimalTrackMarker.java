package nl.erikduisters.pathfinder.ui.fragment.map;

import android.support.annotation.NonNull;

import org.oscim.core.GeoPoint;
import org.oscim.layers.marker.MarkerInterface;
import org.oscim.layers.marker.MarkerSymbol;

import nl.erikduisters.pathfinder.data.model.MinimalTrack;

/**
 * Created by Erik Duisters on 25-08-2018.
 */
public class MinimalTrackMarker implements MarkerInterface {
    @NonNull private MinimalTrack minimalTrack;
    @NonNull private MarkerSymbol markerSymbol;
    private GeoPoint geoPoint;

    MinimalTrackMarker(MinimalTrack minimalTrack, MarkerSymbol markerSymbol) {
        this.minimalTrack = minimalTrack;
        this.markerSymbol = markerSymbol;
        this.geoPoint = new GeoPoint(minimalTrack.startLatitude, minimalTrack.startLongitude);
    }

    @Override
    public MarkerSymbol getMarker() {
        return markerSymbol;
    }

    @Override
    public GeoPoint getPoint() {
        return geoPoint;
    }

    @NonNull
    public MinimalTrack getMinimalTrack() {
        return minimalTrack;
    }
}
