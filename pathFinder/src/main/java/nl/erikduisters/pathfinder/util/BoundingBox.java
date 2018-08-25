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
