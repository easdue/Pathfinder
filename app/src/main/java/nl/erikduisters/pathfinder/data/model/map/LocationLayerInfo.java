package nl.erikduisters.pathfinder.data.model.map;

import android.location.Location;

/**
 * Created by Erik Duisters on 10-07-2018.
 */

public class LocationLayerInfo {
    public boolean hasFix;
    public double latitude;
    public double longitude;
    public float accuracy;
    public boolean hasHeading;
    public float heading;

    public LocationLayerInfo() {
        hasFix = false;
        hasHeading = false;
    }

    public LocationLayerInfo(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        accuracy = location.getAccuracy();
        hasHeading = location.hasBearing();
        heading = location.getBearing();
    }
}
