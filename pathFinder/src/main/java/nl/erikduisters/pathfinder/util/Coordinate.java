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
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Locale;

import nl.erikduisters.pathfinder.R;

import static nl.erikduisters.pathfinder.util.Coordinate.DisplayFormat.FORMAT_DDMMMMM;
import static nl.erikduisters.pathfinder.util.Coordinate.DisplayFormat.FORMAT_DDMMSSS;
import static nl.erikduisters.pathfinder.util.Coordinate.DisplayFormat.FORMAT_DECIMAL;

/**
 * Created by Erik Duisters on 18-07-2018.
 */
public class Coordinate implements Parcelable {
    @IntDef({FORMAT_DECIMAL, FORMAT_DDMMMMM, FORMAT_DDMMSSS})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface DisplayFormat {
        int FORMAT_DECIMAL = 0;
        int FORMAT_DDMMMMM = 1;
        int FORMAT_DDMMSSS = 2;
    }

    //private static DecimalFormat decimalFormat = new DecimalFormat("00.000");
    private static String coordinateIndicators[];
    private static @DisplayFormat int displayFormat = FORMAT_DDMMSSS;

    private double latitude;
    private double longitude;

    public Coordinate() {
        this(-91, -181);
    }

    public Coordinate(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }

    public static void setDisplayFormat(@DisplayFormat int displayFormat) {
        Coordinate.displayFormat = displayFormat;
    }

    private boolean isLatitudeValid() {
        return latitude >= -90 && latitude <= 90;
    }

    private boolean isLongitudeValid() {
        return longitude >= -180 && longitude <= 180;
    }

    public String getLatitudeAsString(Context context) {
        initCoordinateIndicators(context);

        if (!isLatitudeValid()) {
            return "-";
        }

        switch (displayFormat) {
            case FORMAT_DECIMAL:
                return formatDecimal(latitude);
            case FORMAT_DDMMMMM:
                return formatDDMMMMM(latitude, true);
            case FORMAT_DDMMSSS:
                return formatDDMMSSS(latitude, true);
            default:
                throw new IllegalStateException("Invalid displayFormat");
        }
    }

    public String getLongitudeAsString(Context context) {
        initCoordinateIndicators(context);

        if (!isLongitudeValid()) {
            return "-";
        }

        switch (displayFormat) {
            case FORMAT_DECIMAL:
                return formatDecimal(longitude);
            case FORMAT_DDMMMMM:
                return formatDDMMMMM(longitude, false);
            case FORMAT_DDMMSSS:
                return formatDDMMSSS(longitude, false);
            default:
                throw new IllegalStateException("Invalid displayFormat");
        }
    }

    private void initCoordinateIndicators(Context context) {
        if (coordinateIndicators == null) {
            coordinateIndicators = context.getResources().getStringArray(R.array.coordinate_indicators);
        }
    }

    private String formatDecimal(double coord) {
        return Double.toString(coord);
    }

    private String formatDDMMMMM(double coord, boolean isLatitude) {
        StringBuilder sb = new StringBuilder();

        int degrees = (int) coord;
        double remainder = coord - degrees;
        remainder = remainder * 60;

        if (isLatitude) {
            sb.append((degrees > 0) ? coordinateIndicators[0] : coordinateIndicators[1]);
            sb.append(String.format("%02d", Math.abs(degrees)));
        } else {
            sb.append((degrees > 0) ? coordinateIndicators[2] : coordinateIndicators[3]);
            sb.append(String.format("%03d", Math.abs(degrees)));
        }

        sb.append('\u00b0');
        sb.append(String.format(Locale.US, "%02.3f", Math.abs(remainder)));
        //sb.append(decimalFormat.format(Math.abs(remainder)));

        return sb.toString();
    }

    private String formatDDMMSSS(double coord, boolean isLatitude) {
        StringBuilder sb = new StringBuilder();

        int degrees = (int) coord;
        double remainder = Math.abs(coord - degrees);
        int minutes = (int) (remainder * 60);
        float seconds = ((float) remainder - ((float) minutes / 60)) * 3600;

        if (isLatitude) {
            sb.append((degrees > 0) ? coordinateIndicators[0] : coordinateIndicators[1]);
            sb.append(String.format("%02d", Math.abs(degrees)));
        } else {
            sb.append((degrees > 0) ? coordinateIndicators[2] : coordinateIndicators[3]);
            sb.append(String.format("%03d", Math.abs(degrees)));
        }

        sb.append('\u00b0');
        if (minutes < 10) {
            sb.append('0');
        }
        sb.append(minutes);
        sb.append('\'');
        sb.append(String.format("%2.1f", seconds));
        sb.append('"');

        return sb.toString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(this.latitude);
        dest.writeDouble(this.longitude);
    }

    protected Coordinate(Parcel in) {
        this.latitude = in.readDouble();
        this.longitude = in.readDouble();
    }

    public static final Parcelable.Creator<Coordinate> CREATOR = new Parcelable.Creator<Coordinate>() {
        @Override
        public Coordinate createFromParcel(Parcel source) {
            return new Coordinate(source);
        }

        @Override
        public Coordinate[] newArray(int size) {
            return new Coordinate[size];
        }
    };
}
