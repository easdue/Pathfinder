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
