package nl.erikduisters.gpx.util;

/**
 * Created by Erik Duisters on 03-07-2018.
 */
public class TypeUtil {
    private TypeUtil() {}

    public static double assertValidLatitude(double latitude) throws IllegalArgumentException {
        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("Invalid latitude");
        }

        return latitude;
    }

    public static double assertValidLongitude(double longitude) {
        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Invalid longitude");
        }

        return longitude;
    }

    public static float assertValidDegrees(float degrees) {
        if (degrees < 0 || degrees >=360) {
            throw new IllegalArgumentException("Invalid degrees");
        }

        return degrees;
    }

    public static int assertNonNegativeInteger(int number) {
        if (number < 0) {
            throw new IllegalArgumentException("Number cannot be negative");
        }

        return number;
    }

    public static String assertValidFixType(String fixType) {
        switch (fixType) {
            case "none":
            case "2d":
            case "3d":
            case "dgps":
            case "pps":
                return fixType;
            default:
                throw new IllegalArgumentException("Invalid fixType");

        }
    }

    public static int assertValidDgpsId(int dgpsId) {
        if (dgpsId < 0 || dgpsId > 1023) {
            throw new IllegalArgumentException("Invalid dgpsId");
        }

        return dgpsId;
    }

    public static String assertValidProperty(String property) {
        switch (property) {
            case "one-way trip":
            case "round trip":
                return property;
            default:
                throw new IllegalArgumentException("Invalid property");
        }
    }
}
