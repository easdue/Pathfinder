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

package nl.erikduisters.pathfinder.util;

import android.content.Context;

import nl.erikduisters.pathfinder.R;

/**
 * Created by Erik Duisters on 15-07-2018.
 */
public class Distance {
    private static final String UNKNOWN_DISTANCE_STRING = "---";

    public static final double UNKNOWN_DISTANCE = Double.NaN;
    private static @Units int displayUnits = Units.METRIC;

    private final double distanceMeters;
    private final int precision;

    public Distance(double distanceMeters, int precision) {
        this.distanceMeters = distanceMeters;
        this.precision = precision;
    }

    public static void setDisplayUnits(@Units int displayUnits) {
        Distance.displayUnits = displayUnits;
    }

    public String getDistance(Context ctx) {
        return getDistance(ctx, distanceMeters, precision);
    }

    public static String getDistance(Context ctx, double distanceMeters, int precision) {
        String distance;

        if (Double.isNaN(distanceMeters)) {
            return UNKNOWN_DISTANCE_STRING;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("%.");
        sb.append(precision);
        sb.append("f ");

        if (displayUnits == Units.METRIC) {
            if (distanceMeters < 1000) {
                sb.append(ctx.getString(R.string.units_distance_near_metric));
            } else {
                sb.append(ctx.getString(R.string.units_distance_far_metric));
            }
            distance = String.format(sb.toString(), (distanceMeters < 1000) ? distanceMeters : distanceMeters / 1000);
        } else {
            double distanceYards = UnitsUtil.meters2yards(distanceMeters);

            if (distanceYards < 1760) {
                sb.append(ctx.getString(R.string.units_distance_near_imperial));
            } else {
                sb.append(ctx.getString(R.string.units_distance_far_imperial));
            }
            distance = String.format(sb.toString(), (distanceYards < 1760) ? distanceYards : distanceYards / 1760);
        }

        return distance;
    }
}
