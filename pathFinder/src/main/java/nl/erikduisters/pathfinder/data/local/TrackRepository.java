package nl.erikduisters.pathfinder.data.local;

import android.arch.lifecycle.LiveData;
import android.support.annotation.Nullable;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import nl.erikduisters.pathfinder.data.local.database.PathfinderDatabase;
import nl.erikduisters.pathfinder.data.local.database.TrackPoint;
import nl.erikduisters.pathfinder.data.local.database.Waypoint;
import nl.erikduisters.pathfinder.data.model.FullTrack;
import nl.erikduisters.pathfinder.data.model.MinimalTrack;
import nl.erikduisters.pathfinder.util.BoundingBox;
import nl.erikduisters.pathfinder.util.Coordinate;

/**
 * Created by Erik Duisters on 17-08-2018.
 */

@Singleton
public class TrackRepository {
    private final PathfinderDatabase database;

    @Inject
    public TrackRepository(PathfinderDatabase database) {
        this.database = database;
    }

    //TODO: Optimize using Doublas Peucker algorithm
    public void save(FullTrack track) {
        database.beginTransaction();

        database.trackDao().delete(track.gpsiesFileId);
        track.id = database.trackDao().insert(track);

        for (Waypoint waypoint : track.getWaypoints()) {
            waypoint.trackId = track.id;
            waypoint.id = database.waypointDao().insert(waypoint);
        }

        for (int segment = 0, size = track.getTrackSegments().size(); segment < size; segment++) {
            List<TrackPoint> trackPoints = track.getTrackSegments().get(segment).getTrackPoints();

            for (TrackPoint trackPoint : trackPoints) {
                trackPoint.trackId = track.id;
                trackPoint.segment = segment;

                database.trackPointDao().insert(trackPoint);
            }
        }

        database.setTransactionSuccessful();
        database.endTransaction();
    }

    @Nullable
    public FullTrack get(long id) {
        database.beginTransaction();

        FullTrack track = database.trackDao().getTrackById(id);

        if (track == null) {
            return null;
        }

        track.setWaypoints(database.waypointDao().getWaypointsByTrackId(track.id));
        List<TrackPoint> trackPoints = database.trackPointDao().getTrackPointsByTrackId(track.id);

        FullTrack.TrackSegment trackSegment = null;
        int currentSegment = -1;

        for (TrackPoint trackPoint : trackPoints) {
            if (trackSegment == null || trackPoint.segment != currentSegment) {
                trackSegment = new FullTrack.TrackSegment();
                track.getTrackSegments().add(trackSegment);
                currentSegment = trackPoint.segment;
            }

            trackSegment.getTrackPoints().add(trackPoint);
        }

        database.endTransaction();

        return track;
    }

    //TODO: Sort on distance from center
    //TODO: Re-run query when center has changed more than x meters (1000?)
    public LiveData<List<MinimalTrack>> getMinimalTracks(Coordinate center, int radiusMeters, long trackIdToInclude) {
        BoundingBox bb = new BoundingBox(center, radiusMeters);

        return database.trackDao().getMinimalTracks(bb.minLatitude, bb.minLongitude, bb.maxLatitude, bb.maxLongitude, trackIdToInclude);
    }
}
