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
    public boolean hasBearing;
    public float bearing;

    public LocationLayerInfo() {
        hasFix = false;
        hasBearing = false;
    }

    public LocationLayerInfo(Location location) {
        hasFix = true;
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        accuracy = location.getAccuracy();
        hasBearing = location.hasBearing();
        bearing = location.getBearing();
    }

    public LocationLayerInfo(LocationLayerInfo from, boolean hasFix) {
        this.hasFix = hasFix;
        latitude = from.latitude;
        longitude = from.longitude;
        accuracy = hasFix ? from.accuracy : 0f;
        hasBearing = from.hasBearing;
        bearing = from.bearing;
    }
}
