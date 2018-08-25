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
