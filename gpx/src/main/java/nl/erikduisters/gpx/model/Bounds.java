package nl.erikduisters.gpx.model;

import nl.erikduisters.gpx.util.TypeUtil;

/**
 * Created by Erik Duisters on 02-07-2018.
 */
public class Bounds {
    private double minLatitude;
    private double minLongitude;
    private double maxLatitude;
    private double maxLongitude;

    public double getMinLatitude() { return minLatitude; }

    public void setMinLatitude(double minLatitude) { this.minLatitude = TypeUtil.assertValidLatitude(minLatitude); }

    public double getMinLongitude() { return minLongitude; }

    public void setMinLongitude(double minLongitude) { this.minLongitude = TypeUtil.assertValidLongitude(minLongitude); }

    public double getMaxLatitude() { return maxLatitude; }

    public void setMaxLatitude(double maxLatitude) { this.maxLatitude = TypeUtil.assertValidLatitude(maxLatitude); }

    public double getMaxLongitude() { return maxLongitude; }

    public void setMaxLongitude(double maxLongitude) { this.maxLongitude = TypeUtil.assertValidLongitude(maxLongitude); }
}
