package nl.erikduisters.pathfinder.data.local.database;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import nl.erikduisters.pathfinder.data.model.Track;
import nl.erikduisters.pathfinder.data.model.TrackPoint;
import nl.erikduisters.pathfinder.data.model.Waypoint;
import nl.erikduisters.pathfinder.data.model.WaypointType;

/**
 * Created by Erik Duisters on 16-06-2018.
 */
public class TestUtil {
    static Track getTestTrack() {
        Track track = new Track();

        track.name = "Test Track";
        track.description = "This is a test track";
        track.author = "Erik Duisters";
        track.gpsiesId = "dke24l56lsas";
        track.dateCreated = new Date();
        track.type = Track.Type.ONE_WAY;
        track.length = 12.5f;
        track.totalAscent = 10.2f;
        track.totalDescent = 10.4f;
        track.minHeight = 25.0f;
        track.maxHeight = 26.5f;

        return track;
    }

    static TrackPoint getTestTrackPoint() {
        TrackPoint trackPoint = new TrackPoint();
        trackPoint.elevation = 25.1f;
        trackPoint.latitude = 51.34534;
        trackPoint.longitude = 5.654436;
        trackPoint.segment = 0;
        trackPoint.time = new Date();
        trackPoint.trackId = 0;

        return trackPoint;
    }

    static Waypoint getTestWaypoint() {
        Waypoint waypoint = new Waypoint();
        waypoint.name = "Test Waypoint";
        waypoint.description = "This is a Test Waypoint";
        waypoint.elevation = 11.0f;
        waypoint.latitude = 51.23456;
        waypoint.longitude = 5.344365;
        try {
            waypoint.link = new URL("http://www.test.com/somewaypoint");
        } catch (MalformedURLException e) {
            waypoint.link = null;
        }
        waypoint.type = WaypointType.GENERIC;

        return waypoint;
    }
}
