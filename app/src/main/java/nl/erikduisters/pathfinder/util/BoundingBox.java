package nl.erikduisters.pathfinder.util;

import android.os.Parcel;
import android.os.Parcelable;

import org.oscim.core.Box;

/**
 * Created by Erik Duisters on 10-08-2018.
 */
public class BoundingBox implements Parcelable {
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
