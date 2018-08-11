package nl.erikduisters.pathfinder.data.model.map;

import android.location.Location;
import android.support.annotation.Nullable;

/**
 * Created by Erik Duisters on 10-07-2018.
 */

public class LocationLayerInfo {
    public boolean hasFix;
    public double latitude;
    public double longitude;
    public float accuracy;
    public boolean hasBearing;
    public float bearing;

    public LocationLayerInfo(@Nullable Location location) {
        hasFix = true;
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        accuracy = location.getAccuracy();
    }
}
