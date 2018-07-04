package nl.erikduisters.gpx.model;

import nl.erikduisters.gpx.util.TypeUtil;

/**
 * Created by Erik Duisters on 30-06-2018.
 */
public class GpsiesWptExtensions implements Gpx.Extension {
    //These are probably from the gpx 1.0 specification. Gpsies uses meterPerSecond instead of speed
    private float course;
    private float speed;

    public GpsiesWptExtensions() {
        course = Float.NaN;
        speed = Float.NaN;
    }

    public boolean hasCourse() { return !Float.isNaN(course); }

    public float getCourse() { return course; }

    public void setCourse(float course) { this.course = TypeUtil.assertValidDegrees(course); }

    public boolean hasSpeed() { return !Float.isNaN(speed); }

    public float getSpeed() { return speed; }

    public void setSpeed(float speed) { this.speed = speed; }
}
