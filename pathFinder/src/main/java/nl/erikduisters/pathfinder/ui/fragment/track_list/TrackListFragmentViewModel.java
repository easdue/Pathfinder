package nl.erikduisters.pathfinder.ui.fragment.track_list;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import javax.inject.Inject;
import javax.inject.Singleton;

import nl.erikduisters.pathfinder.R;
import nl.erikduisters.pathfinder.data.local.GpsManager;
import nl.erikduisters.pathfinder.data.local.MinimalTrackListLoader;
import nl.erikduisters.pathfinder.data.local.PreferenceManager;
import nl.erikduisters.pathfinder.data.model.MinimalTrack;
import nl.erikduisters.pathfinder.data.model.MinimalTrackList;
import nl.erikduisters.pathfinder.ui.fragment.track_list.TrackListFragmentViewState.DataState;
import nl.erikduisters.pathfinder.ui.fragment.track_list.TrackListFragmentViewState.NoTracksFoundState;
import nl.erikduisters.pathfinder.util.Distance;
import nl.erikduisters.pathfinder.util.SingleSourceMediatorLiveData;
import timber.log.Timber;

/**
 * Created by Erik Duisters on 28-06-2018.
 */
//TODO: I've seen it happen that when you import tracks with an empty database that room does not trigger a re-query and you end up with an empty track list until you restart/orientation change. Maybe send a broadcast from TrackImportService or something
//TODO: Subscribe to GpsManager and resort MinimalTrackList on every meter moved
//TODO: Long press starts contextual action mode to allow the user to delete tracks, upload tracks to GPSies.com or Share tracks
@Singleton
public class TrackListFragmentViewModel extends ViewModel implements GpsManager.LocationListener, SingleSourceMediatorLiveData.Listener {
    private final PreferenceManager preferenceManager;
    private final GpsManager gpsManager;
    private final MinimalTrackListLoader minimalTrackListLoader;
    private final SingleSourceMediatorLiveData<TrackListFragmentViewState> viewStateObservable;
    private long selectedTrackId;
    private Location sortLocation;

    @Inject
    TrackListFragmentViewModel(PreferenceManager preferenceManager, GpsManager gpsManager, MinimalTrackListLoader minimalTrackListLoader) {
        Timber.d("New TrackListFragmentViewModel created");

        this.preferenceManager = preferenceManager;
        this.gpsManager = gpsManager;
        this.minimalTrackListLoader = minimalTrackListLoader;
        selectedTrackId = 0;
        sortLocation = null;

        viewStateObservable = new SingleSourceMediatorLiveData<>();
        viewStateObservable.setListener(this);
        viewStateObservable.addSource(minimalTrackListLoader.getMinimalTrackListObservable(), this::minimalTrackListToViewState);
    }

    /* First attempt to get livedata updates working

    private final MutableLiveData<TrackListFragmentViewState> realViewStateObservable;
    private final LiveData<TrackListFragmentViewState> viewStateObservable;

    this.realViewStateObservable = new MutableLiveData<>();
    this.viewStateObservable = Transformations.switchMap(minimalTrackListLoader.getMinimalTrackListObservable(), this::minimalTrackListToViewState);
    this.realViewStateObservable.setValue(new TrackListFragmentViewState.LoadingState(R.string.loading_tracks));


    private LiveData<TrackListFragmentViewState> minimalTrackListToViewState(MinimalTrackList minimalTrackList) {
        if (minimalTrackList.getMinimalTracks().size() == 0) {
            Distance distance = new Distance(preferenceManager.getTrackLoadRadius(), 0);

            if (preferenceManager.mapFollowsGps()) {
                realViewStateObservable.setValue(
                        new NoTracksFoundState(R.string.no_tracks_found_within_radius_from_current_location, distance));
            } else {
                realViewStateObservable.setValue(
                        new NoTracksFoundState(R.string.no_tracks_found_within_radius_from_current_map_center, distance));
            }
        } else {
            TrackListFragmentViewState currentViewState = viewStateObservable.getValue();

            MinimalTrack selectedMinimalTrack = null;

            if (currentViewState instanceof TrackListFragmentViewState.DataState) {
                selectedMinimalTrack = ((TrackListFragmentViewState.DataState) currentViewState).selectedMinimalTrack;
            }

            realViewStateObservable.setValue(new TrackListFragmentViewState.DataState(minimalTrackList, selectedMinimalTrack));
        }

        return realViewStateObservable;
    }
    */

    private void minimalTrackListToViewState(MinimalTrackList minimalTrackList) {
        if (minimalTrackList.getMinimalTracks().size() == 0) {
            sortLocation = null;

            Distance distance = new Distance(preferenceManager.getTrackLoadRadius(), 0);

            if (preferenceManager.mapFollowsGps()) {
                viewStateObservable.setValue(
                        new NoTracksFoundState(R.string.no_tracks_found_within_radius_from_current_location, distance));
            } else {
                viewStateObservable.setValue(
                        new NoTracksFoundState(R.string.no_tracks_found_within_radius_from_current_map_center, distance));
            }
        } else {
            sortLocation = minimalTrackList.getCenter();

            MinimalTrack selectedMinimalTrack = null;

            if (selectedTrackId > 0) {
                for (MinimalTrack minimalTrack : minimalTrackList.getMinimalTracks()) {
                    if (minimalTrack.id == selectedTrackId) {
                        selectedMinimalTrack = minimalTrack;
                        break;
                    }
                }
            }

            viewStateObservable.setValue(new DataState(minimalTrackList, selectedMinimalTrack));
        }
    }

    LiveData<TrackListFragmentViewState> getViewStateObservable() {
        if (viewStateObservable.getValue() == null) {
            minimalTrackListLoader.loadMinimalTrackList(selectedTrackId);

            viewStateObservable.setValue(new TrackListFragmentViewState.LoadingState(R.string.loading_tracks));
        }

        return viewStateObservable;
    }

    @NonNull
    private DataState getCurrentDataState() {
        TrackListFragmentViewState currentState = viewStateObservable.getValue();

        if (!(currentState instanceof DataState)) {
            throw new IllegalStateException("The current viewstate should be DataState");
        }

        return (DataState) currentState;
    }

    void onMinimalTrackClicked(MinimalTrack minimalTrack) {
        DataState currentState = getCurrentDataState();

        selectedTrackId = minimalTrack.id;
        viewStateObservable.setValue(new DataState(currentState.minimalTrackList, minimalTrack));

        //TODO: Kickoff loading of FullTrack
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        if (!(viewStateObservable.getValue() instanceof DataState)) {
            return;
        }

        //TODO: What distance to use? When walking 10?, when cycling ?, when driving ?
        if (sortLocation.distanceTo(location) >= 10.0f) {
            Timber.d("Location changed more than 10 meters, resorting minimalCacheList");

            DataState currentState = getCurrentDataState();
            currentState.minimalTrackList.sortByDistance(location);
            sortLocation = location;
            viewStateObservable.setValue(new DataState(currentState.minimalTrackList, currentState.selectedMinimalTrack));
        }
    }

    @Override
    public void onActive() {
        gpsManager.addLocationListener(this);
    }

    @Override
    public void onInactive() {
        gpsManager.removeLocationListener(this);
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        viewStateObservable.setValue(null);
    }

    Parcelable onSaveState() {
        if (selectedTrackId > 0) {
            return new SavedState(selectedTrackId);
        }

        return null;
    }

    /**
     * <p>Restores saved state</p>
     *
     * <b>NOTE:</b> Must be called before calling getViewStateObservable()
     * @param savedState The state saved by calling onSaveState()
     */
    void onRestoreState(Parcelable savedState) {
        if (savedState == null || viewStateObservable.getValue() != null) {
            Timber.d("Not restoring state");
            return;
        }

        if (savedState instanceof SavedState) {
            selectedTrackId = ((SavedState) savedState).selectedTrackId;
        }
    }

    private static class SavedState implements Parcelable {
        long selectedTrackId;

        SavedState(long selectedTrackId) {
            this.selectedTrackId = selectedTrackId;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeLong(this.selectedTrackId);
        }

        protected SavedState(Parcel in) {
            this.selectedTrackId = in.readLong();
        }

        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel source) {
                return new SavedState(source);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}
