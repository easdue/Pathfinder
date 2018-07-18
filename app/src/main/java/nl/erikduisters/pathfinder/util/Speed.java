package nl.erikduisters.pathfinder.util;

import android.content.Context;

import nl.erikduisters.pathfinder.R;

/**
 * Created by Erik Duisters on 15-07-2018.
 */
public class Speed {
    private static final String UNKNOWN_SPEED_STRING = "---";
    public static final double UNKNOWN_SPEED = -1;

    private static @Units int displayUnits = Units.METRIC;

    private final double speedKmh;
    private final int precision;

    public Speed(double speedKmh, int precision) {
        this.speedKmh = speedKmh;
        this.precision = precision;
    }

    public static void setDisplayUnits(@Units int displayUnits) {
        Speed.displayUnits = displayUnits;
    }

    public String getSpeed(Context ctx) {
        String speed;

        StringBuilder sb = new StringBuilder();

        if (speedKmh == UNKNOWN_SPEED) {
            sb.append(UNKNOWN_SPEED_STRING)
              .append(" ");
        } else {
            sb.append("%.");
            sb.append(precision);
            sb.append("f ");
        }

        if (displayUnits == Units.METRIC) {
            sb.append(ctx.getString(R.string.units_speed_metric));
            speed = String.format(sb.toString(), speedKmh);
        } else {
            sb.append(ctx.getString(R.string.units_speed_imperial));
            speed = String.format(sb.toString(), UnitsUtil.kilometers2Miles(speedKmh));
        }

        return speed;
    }
}
