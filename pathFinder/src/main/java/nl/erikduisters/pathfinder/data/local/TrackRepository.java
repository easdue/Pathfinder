/*
 * Copyright (c) 2018 Erik Duisters
 *
 * This file is part of Pathfinder
 *
 * Pathfinder is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Foobar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Pathfinder. If not, see <https://www.gnu.org/licenses/>.
 */

package nl.erikduisters.pathfinder.data.local;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.location.Location;
import android.support.annotation.MainThread;
import android.support.annotation.Nullable;

import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import nl.erikduisters.pathfinder.data.local.database.PathfinderDatabase;
import nl.erikduisters.pathfinder.data.local.database.TrackPoint;
import nl.erikduisters.pathfinder.data.local.database.Waypoint;
import nl.erikduisters.pathfinder.data.model.FullTrack;
import nl.erikduisters.pathfinder.data.model.MinimalTrack;
import nl.erikduisters.pathfinder.data.model.MinimalTrackList;
import nl.erikduisters.pathfinder.util.BoundingBox;
import nl.erikduisters.pathfinder.util.SingleSourceMediatorLiveData;

/**
 * Created by Erik Duisters on 17-08-2018.
 */

@Singleton
public class TrackRepository {
    private final PathfinderDatabase database;
    private final SingleSourceMediatorLiveData<MinimalTrackList> minimalTrackListLiveData;

    @Inject
    public TrackRepository(PathfinderDatabase database) {
        this.database = database;
        this.minimalTrackListLiveData = new SingleSourceMediatorLiveData<>();
    }

    //TODO: Optimize using Doublas Peucker algorithm
    public void save(FullTrack track) {
        database.beginTransaction();

        try {
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
        } finally {
            database.endTransaction();
        }
    }

    @Nullable
    public FullTrack get(long id) {
        database.beginTransaction();

        try {
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

            return track;
        } finally {
            database.endTransaction();
        }
    }

    @MainThread
    public LiveData<MinimalTrackList> getMinimalTrackList(Location center, int radiusMeters, long idOfTrackToInclude) {
        BoundingBox bb = new BoundingBox(center, radiusMeters);

        LiveData<List<MinimalTrack>> minimalTracksLiveData = database.trackDao().getMinimalTracks(bb.minLatitude, bb.minLongitude, bb.maxLatitude, bb.maxLongitude, idOfTrackToInclude);
        return Transformations.switchMap(minimalTracksLiveData, trackList -> ListOfMinimalTracksToMinimalTrackList(trackList, center, radiusMeters, idOfTrackToInclude));
    }

    @MainThread
    private LiveData<MinimalTrackList> ListOfMinimalTracksToMinimalTrackList(List<MinimalTrack> trackList, Location center, int radiusMeters, long idOfTrackToInclude) {
        Iterator<MinimalTrack> it = trackList.iterator();

        Location trackLocation = new Location("");

        while (it.hasNext()) {
            MinimalTrack minimalTrack = it.next();
            trackLocation.setLatitude(minimalTrack.startLatitude);
            trackLocation.setLongitude(minimalTrack.startLongitude);

            //TODO: Maybe use spherical formula to calculate distance (See vtm:GeoPoint) which is faster but less accurate
            if (minimalTrack.id != idOfTrackToInclude && center.distanceTo(trackLocation) > radiusMeters) {
                it.remove();
            } else {
                minimalTrack.distanceTo = center.distanceTo(trackLocation);
                minimalTrack.initialBearingTo = center.bearingTo(trackLocation);
            }
        }

        MinimalTrackList minimalTrackList = new MinimalTrackList(trackList, center);
        minimalTrackList.sortByDistance();

        MutableLiveData<MinimalTrackList> minimalTrackListObservable = new MutableLiveData<>();
        minimalTrackListObservable.setValue(minimalTrackList);

        return minimalTrackListObservable;
    }
}
