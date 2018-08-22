package nl.erikduisters.pathfinder.data.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Erik Duisters on 17-08-2018.
 */
public enum TrackType implements Parcelable {
    ROUND_TRIP(1, 1, "round trip"),
    ONE_WAY(2, 2, "one-way trip");

    final int code;
    final int markersProperty;
    final String gpxProperty;

    private static final TrackType[] values;

    static {
        values = TrackType.values();
    }

    TrackType(int code, int markersProperty, String gpxProperty) {
        this.code = code;
        this.markersProperty = markersProperty;
        this.gpxProperty = gpxProperty;
    }

    public int code() { return code; }
    public int getMarkersProperty() { return markersProperty; }
    public String getGpxProperty() { return gpxProperty; }

    public static TrackType fromCode(int code) {
        for (TrackType trackType : values) {
            if (trackType.code == code) {
                return trackType;
            }
        }

        throw new IllegalArgumentException("There is no TrackType with code: " + code);
    }

    public static TrackType fromMarkersProperty(int markersProperty) {
        for (TrackType trackType : values) {
            if (trackType.markersProperty == markersProperty) {
                return trackType;
            }
        }

        throw new IllegalArgumentException("There is no TrackType with markersProperty: " + markersProperty);
    }

    public static TrackType fromGpxProperty(String gpxProperty) {
        for (TrackType trackType : values) {
            if (trackType.gpxProperty.equals(gpxProperty)) {
                return trackType;
            }
        }

        throw new IllegalArgumentException("There is no TrackType with gpxProperty: " + gpxProperty);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(code);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<TrackType> CREATOR = new Creator<TrackType>() {
        @Override
        public TrackType createFromParcel(Parcel in) {
            int code = in.readInt();
            return TrackType.fromCode(code);
        }

        @Override
        public TrackType[] newArray(int size) {
            return new TrackType[size];
        }
    };
}
