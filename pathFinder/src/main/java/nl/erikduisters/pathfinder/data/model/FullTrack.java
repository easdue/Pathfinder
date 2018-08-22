package nl.erikduisters.pathfinder.data.model;

import android.arch.persistence.room.Ignore;
import android.support.annotation.NonNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import nl.erikduisters.gpx.model.GpsiesMetaDataExtensions;
import nl.erikduisters.gpx.model.Gpx;
import nl.erikduisters.gpx.model.Link;
import nl.erikduisters.gpx.model.Metadata;
import nl.erikduisters.gpx.model.Track;
import nl.erikduisters.pathfinder.data.local.database.TrackPoint;
import nl.erikduisters.pathfinder.data.local.database.Waypoint;

/**
 * Created by Erik Duisters on 17-08-2018.
 */
public class FullTrack extends nl.erikduisters.pathfinder.data.local.database.Track {
    @NonNull @Ignore List<Waypoint> waypoints;
    @NonNull @Ignore List<TrackSegment> trackSegments;

    public FullTrack() {
        waypoints = new ArrayList<>();
        trackSegments = new ArrayList<>();
    }

    public FullTrack(@NonNull Gpx gpx) throws IllegalArgumentException {
        this();

        if (gpx.getTracks().isEmpty() || gpx.getTracks().size() > 1) {
            throw new IllegalArgumentException("The gpx must contain exactly 1 track");
        }

        if (gpx.getMetadata() == null) {
            throw new IllegalArgumentException("The gpx does not have metadata");
        }

        addFromMetaData(gpx.getMetadata());
        addTrackWaypoints(gpx.getWaypoints());
        addFromTrack(gpx.getTracks().get(0));
    }

    private void addFromMetaData(Metadata metadata) {
        this.name = metadata.getName();
        this.description = metadata.getDescription();
        this.author = metadata.getAuthor().getName();
        this.dateCreated = metadata.getTime();

        getGpsiesFileIdFromLinks(metadata.getLinks());

        addFromMetaDataExtensions(metadata.getExtensions());
    }

    private void getGpsiesFileIdFromLinks(List<Link> links) {
        for (Link link : links) {
            int index = link.getHref().indexOf("fileId=");

            if (index >= 0) {
                this.gpsiesFileId = link.getHref().substring(index + 7);
                break;
            }
        }
    }

    private void addFromMetaDataExtensions(List<Gpx.Extension> extensions) {
        for (Gpx.Extension extension : extensions) {
            if (extension instanceof GpsiesMetaDataExtensions) {
                GpsiesMetaDataExtensions gpsiesExtension = (GpsiesMetaDataExtensions) extension;
                this.type = TrackType.fromGpxProperty(gpsiesExtension.getProperty());
                this.length = gpsiesExtension.getTrackLengthMeter();
                this.totalAscent = gpsiesExtension.getTotalAscentMeter();
                this.totalDescent = gpsiesExtension.getTotalDescentMeter();
                this.minHeight = gpsiesExtension.getMinHeightMeter();
                this.maxHeight = gpsiesExtension.getMaxHeightMeter();

                break;
            }
        }
    }

    private void addTrackWaypoints(List<nl.erikduisters.gpx.model.Waypoint> gpxWaypoints) {
        for (nl.erikduisters.gpx.model.Waypoint gpxWaypoint : gpxWaypoints) {
            Waypoint waypoint = new Waypoint();

            waypoint.description = gpxWaypoint.getDescription();
            waypoint.elevation = gpxWaypoint.getElevation();
            waypoint.latitude = gpxWaypoint.getLatitude();
            waypoint.longitude = gpxWaypoint.getLongitude();
            if (!gpxWaypoint.getLinks().isEmpty()) {
                try {
                    waypoint.link = new URL(gpxWaypoint.getLinks().get(0).getHref());
                } catch (MalformedURLException e) {
                    //To bad
                }
            }
            waypoint.name = gpxWaypoint.getName();
            waypoint.type = WaypointType.fromGpxType(gpxWaypoint.getType());

            waypoints.add(waypoint);
        }
    }

    private void addFromTrack(Track track) {
        if (gpsiesFileId == null || gpsiesFileId.isEmpty()) {
            getGpsiesFileIdFromLinks(track.getLinks());
        }

        for (nl.erikduisters.gpx.model.TrackSegment trackSegment : track.getTrackSegments()) {
            TrackSegment currentSegment = new TrackSegment();

            addTrackPointsFromTrackSegment(trackSegment, currentSegment);

            this.trackSegments.add(currentSegment);
        }
    }

    private void addTrackPointsFromTrackSegment(nl.erikduisters.gpx.model.TrackSegment from, TrackSegment to) {
        for (nl.erikduisters.gpx.model.Waypoint waypoint : from.getTrackPoints()) {
            TrackPoint trackPoint = new TrackPoint();
            trackPoint.latitude = waypoint.getLatitude();
            trackPoint.longitude = waypoint.getLongitude();
            trackPoint.elevation = waypoint.getElevation();
            trackPoint.time = waypoint.getTime();

            to.trackPoints.add(trackPoint);
        }
    }

    public @NonNull List<Waypoint> getWaypoints() { return waypoints; }
    public void setWaypoints(@NonNull List<Waypoint> waypoints) { this.waypoints = waypoints; }
    public @NonNull List<TrackSegment> getTrackSegments() { return trackSegments; }
    public void setTrackSegments(@NonNull List<TrackSegment> trackSegments) { this.trackSegments = trackSegments; }

    public static class TrackSegment {
        private @NonNull List<TrackPoint> trackPoints;

        public TrackSegment() {
            trackPoints = new ArrayList<>();
        }

        public List<TrackPoint> getTrackPoints() { return trackPoints; }
    }
}
