package nl.erikduisters.pathfinder.util;

import android.content.Context;

import nl.erikduisters.pathfinder.R;

/**
 * Created by Erik Duisters on 15-07-2018.
 */
public class Distance {
    private static final String UNKNOWN_DISTANCE_STRING = "---";

    public static final double UNKNOWN_DISTANCE = -1;

    private final double distanceMeters;
    private final int precision;
    private final @Units int displayUnits;

    public Distance(double distanceMeters, int precision, @Units int displayUnits) {
        this.distanceMeters = distanceMeters;
        this.precision = precision;
        this.displayUnits = displayUnits;
    }

    public String getDistance(Context ctx) {
        String distance;

        if (distanceMeters == UNKNOWN_DISTANCE) {
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
