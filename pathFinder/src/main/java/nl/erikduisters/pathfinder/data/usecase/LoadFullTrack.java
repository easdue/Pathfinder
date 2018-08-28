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

package nl.erikduisters.pathfinder.data.usecase;

import android.support.annotation.NonNull;

import nl.erikduisters.pathfinder.async.Cancellable;
import nl.erikduisters.pathfinder.data.local.TrackRepository;
import nl.erikduisters.pathfinder.data.model.FullTrack;

/**
 * Created by Erik Duisters on 25-08-2018.
 */
public class LoadFullTrack extends UseCase<LoadFullTrack.RequestInfo, FullTrack> {
    public LoadFullTrack(@NonNull RequestInfo requestInfo, @NonNull Callback<FullTrack> callback) {
        super(requestInfo, callback);
    }

    @Override
    public void execute(Cancellable cancellable) {
        try {
            FullTrack fullTrack = requestInfo.trackRepository.get(requestInfo.trackId);

            callback.onResult(fullTrack);
        } catch (Exception e) {
            callback.onError(e);
        }
    }

    public static class RequestInfo {
        @NonNull private final TrackRepository trackRepository;
        private final long trackId;

        public RequestInfo(@NonNull TrackRepository trackRepository, long trackId) {
            this.trackRepository = trackRepository;
            this.trackId = trackId;
        }
    }
}
