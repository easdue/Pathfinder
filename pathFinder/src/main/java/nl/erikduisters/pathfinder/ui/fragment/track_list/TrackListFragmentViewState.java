package nl.erikduisters.pathfinder.ui.fragment.track_list;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import nl.erikduisters.pathfinder.data.model.MinimalTrack;
import nl.erikduisters.pathfinder.data.model.MinimalTrackList;
import nl.erikduisters.pathfinder.util.Distance;

/**
 * Created by Erik Duisters on 28-06-2018.
 */
interface TrackListFragmentViewState {
    class LoadingState implements TrackListFragmentViewState {
        final @StringRes int message;

        LoadingState(@StringRes int message) {
            this.message = message;
        }
    }

    class NoTracksFoundState implements TrackListFragmentViewState {
        final @StringRes int messageResId;
        final @NonNull Distance distance;

        NoTracksFoundState(@StringRes int messageResId, @NonNull Distance distance) {
            this.messageResId = messageResId;
            this.distance = distance;
        }
    }

    class DataState implements TrackListFragmentViewState {
        final @NonNull MinimalTrackList minimalTrackList;
        final @Nullable MinimalTrack selectedMinimalTrack;

        DataState(@NonNull MinimalTrackList minimalTrackList, @Nullable MinimalTrack selectedMinimalTrack) {
            this.minimalTrackList = minimalTrackList;
            this.selectedMinimalTrack = selectedMinimalTrack;
        }
    }
}
