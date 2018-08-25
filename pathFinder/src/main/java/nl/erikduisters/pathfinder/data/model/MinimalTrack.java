package nl.erikduisters.pathfinder.data.model;

import android.arch.persistence.room.Ignore;

/**
 * Created by Erik Duisters on 17-08-2018.
 */
public class MinimalTrack {
    public long id;
    public String gpsiesFileId;
    public double startLatitude;
    public double startLongitude;
    public String name;
    public String author;
    public TrackType type;
    public float length;
    public float totalAscent;
    public float totalDescent;
    public int numWaypoints;
    public int numTrackPoints;
    @Ignore
    public float distanceTo;
    @Ignore
    public float initialBearingTo;
}
