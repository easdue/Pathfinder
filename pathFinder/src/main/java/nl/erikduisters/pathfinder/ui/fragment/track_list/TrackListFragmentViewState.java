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
