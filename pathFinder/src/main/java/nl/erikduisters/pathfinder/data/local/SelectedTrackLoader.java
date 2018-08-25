package nl.erikduisters.pathfinder.data.local;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.crashlytics.android.Crashlytics;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import nl.erikduisters.pathfinder.async.BackgroundJob;
import nl.erikduisters.pathfinder.async.BackgroundJobHandler;
import nl.erikduisters.pathfinder.data.model.FullTrack;
import nl.erikduisters.pathfinder.data.usecase.LoadFullTrack;

/**
 * Created by Erik Duisters on 25-08-2018.
 */
@Singleton
public class SelectedTrackLoader {
    public interface Listener {
        void onFullTrackLoaded(@Nullable FullTrack fullTrack);
    }

    private final TrackRepository trackRepository;
    private final BackgroundJobHandler backgroundJobHandler;
    private final Callback callback;
    private final List<Listener> listeners;
    @Nullable private BackgroundJob currentJob;

    @Inject
    SelectedTrackLoader(TrackRepository trackRepository, BackgroundJobHandler backgroundJobHandler) {
        this.trackRepository = trackRepository;
        this.backgroundJobHandler = backgroundJobHandler;
        this.callback = new Callback();
        this.listeners = new ArrayList<>();
    }

    public void addListener(Listener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    public void loadTrackWithId(long id) {
        if (currentJob != null) {
            currentJob.cancel();
        }

        LoadFullTrack.RequestInfo requestInfo = new LoadFullTrack.RequestInfo(trackRepository, id);
        LoadFullTrack useCase = new LoadFullTrack(requestInfo, callback);

        currentJob = useCase.getUseCaseJob();
        backgroundJobHandler.runJob(currentJob);
    }

    private class Callback implements LoadFullTrack.Callback<FullTrack> {
        @Override
        public void onResult(@Nullable FullTrack result) {
            currentJob = null;

            for (Listener listener : listeners) {
                listener.onFullTrackLoaded(result);
            }
        }

        @Override
        public void onError(@NonNull Throwable error) {
            currentJob = null;

            Crashlytics.logException(error);
        }
    }
}
