package nl.erikduisters.gpx.model;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Erik Duisters on 02-07-2018.
 */
public class TrackSegment implements WaypointsContainer, ExtensionsContainer {
    @NonNull private List<Waypoint> trackPoints;
    @NonNull private List<Gpx.Extension> extensions;

    public TrackSegment() {
        trackPoints = new ArrayList<>();
        extensions = new ArrayList<>();
    }

    @Override
    public List<Gpx.Extension> getExtensions() {
        return extensions;
    }

    public List<Waypoint> getTrackPoints() { return trackPoints; }

    @Override
    public List<Waypoint> getWaypoints() {
        return trackPoints;
    }
}
