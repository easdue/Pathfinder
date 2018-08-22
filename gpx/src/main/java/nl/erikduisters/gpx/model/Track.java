package nl.erikduisters.gpx.model;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Erik Duisters on 02-07-2018.
 */
public class Track extends RouteOrTrack {
    @NonNull private List<TrackSegment> trackSegments;

    public Track() {
        super();

        trackSegments = new ArrayList<>();
    }

    public List<TrackSegment> getTrackSegments() { return trackSegments; }
}
