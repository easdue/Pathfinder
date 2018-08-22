package nl.erikduisters.pathfinder.data.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.squareup.moshi.Json;

/**
 * Created by Erik Duisters on 03-05-2018.
 */
public class Marker implements Parcelable {
    public double startpointLat;
    public double startpointLon;
    public String filename;
    public String fileId;
    public int dataType;
    public TrackType property;
    @Json(name = "maxHeight")
    public float maxHeightMeters;
    @Json(name = "minHeight")
    public float minHeightMeters;
    @Json(name = "totalAscent")
    public float totalAscentMeters;
    @Json(name = "totalDescent")
    public float totalDescentMeters;
    @Json(name = "trackLength")
    public float trackLengthKilometers;
    public String websiteUrl;
    public String websiteUrlShort;
    public String bestTrackType;
    public int index;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(this.startpointLat);
        dest.writeDouble(this.startpointLon);
        dest.writeString(this.filename);
        dest.writeString(this.fileId);
        dest.writeInt(this.dataType);
        dest.writeParcelable(property, flags);
        dest.writeFloat(this.maxHeightMeters);
        dest.writeFloat(this.minHeightMeters);
        dest.writeFloat(this.totalAscentMeters);
        dest.writeFloat(this.totalDescentMeters);
        dest.writeFloat(this.trackLengthKilometers);
        dest.writeString(this.websiteUrl);
        dest.writeString(this.websiteUrlShort);
        dest.writeString(this.bestTrackType);
        dest.writeInt(this.index);
    }

    public Marker() {
    }

    protected Marker(Parcel in) {
        this.startpointLat = in.readDouble();
        this.startpointLon = in.readDouble();
        this.filename = in.readString();
        this.fileId = in.readString();
        this.dataType = in.readInt();
        this.property = in.readParcelable(TrackType.class.getClassLoader());
        this.maxHeightMeters = in.readFloat();
        this.minHeightMeters = in.readFloat();
        this.totalAscentMeters = in.readFloat();
        this.totalDescentMeters = in.readFloat();
        this.trackLengthKilometers = in.readFloat();
        this.websiteUrl = in.readString();
        this.websiteUrlShort = in.readString();
        this.bestTrackType = in.readString();
        this.index = in.readInt();
    }

    public static final Creator<Marker> CREATOR = new Creator<Marker>() {
        @Override
        public Marker createFromParcel(Parcel source) {
            return new Marker(source);
        }

        @Override
        public Marker[] newArray(int size) {
            return new Marker[size];
        }
    };
}
