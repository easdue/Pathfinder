package nl.erikduisters.pathfinder.util;

import android.content.Context;

import nl.erikduisters.pathfinder.R;

/**
 * Created by Erik Duisters on 15-07-2018.
 */
public class Speed {
    private final double speedKmh;
    private final int precision;
    private final @Units int displayUnits;

    public Speed(double speedKmh, int precision, @Units int displayUnits) {
        this.speedKmh = speedKmh;
        this.precision = precision;
        this.displayUnits = displayUnits;
    }

    public String getSpeed(Context ctx) {
        String speed;

        StringBuilder sb = new StringBuilder();
        sb.append("%.");
        sb.append(precision);
        sb.append("f ");

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
