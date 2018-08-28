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

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import org.oscim.core.Box;

/**
 * Created by Erik Duisters on 10-08-2018.
 */
public class BoundingBox implements Parcelable {
    private static final double majorEarthRadiusMeters = 6378137d;

    public final double minLatitude;
    public final double minLongitude;
    public final double maxLatitude;
    public final double maxLongitude;

    public BoundingBox(double minLatitude, double minLongitude, double maxLatitude, double maxLongitude) {
        this.minLatitude = minLatitude;
        this.maxLatitude = maxLatitude;
        this.minLongitude = minLongitude;
        this.maxLongitude = maxLongitude;
    }

    public BoundingBox(Box box) {
        box.map2mercator();

        minLatitude = box.ymin;
        minLongitude = box.xmin;
        maxLatitude = box.ymax;
        maxLongitude = box.xmax;
    }

    /**
     * @param center The center point of the bounding box
     * @param radius The radius from the center point the bounding box must enclose in meters
     */
    public BoundingBox(Location center, int radius) {
        /*
         * Based on http://janmatuschek.de/LatitudeLongitudeBoundingCoordinates
         */

        if (radius < 0d)
            throw new IllegalArgumentException();

        // angular distance in radians on a great circle
        double distRad = radius / majorEarthRadiusMeters;
        double latRad = Math.toRadians(center.getLatitude());
        double longRad = Math.toRadians(center.getLongitude());

        double minLatRad = latRad - distRad;
        double maxLatRad = latRad + distRad;

        double minLonRad, maxLonRad;
        if (minLatRad > Math.toRadians(-90d) && maxLatRad < Math.toRadians(90d)) {
            double deltaLonRad = Math.asin(Math.sin(distRad) / Math.cos(latRad));
            minLonRad = longRad - deltaLonRad;
            if (minLonRad < Math.toRadians(-180d)) minLonRad += 2d * Math.PI;
            maxLonRad = longRad + deltaLonRad;
            if (maxLonRad > Math.toRadians(180d)) maxLonRad -= 2d * Math.PI;
        } else {
            // a pole is within the distance
            minLatRad = Math.max(minLatRad, Math.toRadians(-90d));
            maxLatRad = Math.min(maxLatRad, Math.toRadians(90d));
            minLonRad = Math.toRadians(-180d);
            maxLonRad = Math.toRadians(180d);
        }

        this.minLatitude = Math.toDegrees(minLatRad);
        this.minLongitude = Math.toDegrees(minLonRad);
        this.maxLatitude = Math.toDegrees(maxLatRad);
        this.maxLongitude = Math.toDegrees(maxLonRad);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(this.minLatitude);
        dest.writeDouble(this.minLongitude);
        dest.writeDouble(this.maxLatitude);
        dest.writeDouble(this.maxLongitude);
    }

    protected BoundingBox(Parcel in) {
        this.minLatitude = in.readDouble();
        this.minLongitude = in.readDouble();
        this.maxLatitude = in.readDouble();
        this.maxLongitude = in.readDouble();
    }

    public static final Parcelable.Creator<BoundingBox> CREATOR = new Parcelable.Creator<BoundingBox>() {
        @Override
        public BoundingBox createFromParcel(Parcel source) {
            return new BoundingBox(source);
        }

        @Override
        public BoundingBox[] newArray(int size) {
            return new BoundingBox[size];
        }
    };
}
