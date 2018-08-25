package nl.erikduisters.pathfinder.data.model;

import android.location.Location;
import android.support.annotation.NonNull;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Erik Duisters on 23-08-2018.
 */
//TODO: Add lock because an instance of this class will be used by TrackListFragment and MapFragment and MapFragment accesses it on a background thread
public class MinimalTrackList {
    @NonNull private final List<MinimalTrack> minimalTracks;
    @NonNull private final Location center;

    public MinimalTrackList(@NonNull List<MinimalTrack> minimalTracks, @NonNull Location center) {
        this.minimalTracks = minimalTracks;
        this.center = center;
    }

    @NonNull
    public List<MinimalTrack> getMinimalTracks() {
        return minimalTracks;
    }

    @NonNull
    public Location getCenter() {
        return center;
    }

    public void sortByDistance() {
        Collections.sort(minimalTracks, new CompareMinimalTracksByDistance());
    }

    public void sortByDistance(Location location) {
        float[] results = new float[2];

        for (MinimalTrack minimalTrack : minimalTracks) {
            Location.distanceBetween(location.getLatitude(), location.getLongitude(), minimalTrack.startLatitude, minimalTrack.startLongitude, results);
            minimalTrack.distanceTo = results[0];
            minimalTrack.initialBearingTo = results[1];
        }

        sortByDistance();
    }

    private class CompareMinimalTracksByDistance implements Comparator<MinimalTrack> {
        @Override
        public int compare(MinimalTrack track1, MinimalTrack track2) {
            if (track1.distanceTo == track2.distanceTo) {
                int res = track1.name.compareTo(track2.name);

                if (res < 0) {
                    return -1;
                } else if (res > 0) {
                    return 1;
                } else return 0;
            }

            if (track1.distanceTo < track2.distanceTo) {
                return -1;
            } else {
                return 1;
            }
        }
    }
}
