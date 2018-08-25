package nl.erikduisters.pathfinder.ui.dialog.select_tracks_to_import;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import com.crashlytics.android.Crashlytics;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import nl.erikduisters.pathfinder.R;
import nl.erikduisters.pathfinder.data.model.Marker;
import nl.erikduisters.pathfinder.service.gpsies_service.Result;
import nl.erikduisters.pathfinder.service.gpsies_service.SearchTracks;
import nl.erikduisters.pathfinder.ui.dialog.select_tracks_to_import.SelectTracksToImportDialogViewState.DataState;
import nl.erikduisters.pathfinder.ui.dialog.select_tracks_to_import.SelectTracksToImportDialogViewState.DisplayMessageState;
import nl.erikduisters.pathfinder.ui.dialog.select_tracks_to_import.SelectTracksToImportDialogViewState.DisplayShortMessageState;
import nl.erikduisters.pathfinder.ui.dialog.select_tracks_to_import.SelectTracksToImportDialogViewState.DisplayShortMessageState.DisplayDuration;
import nl.erikduisters.pathfinder.ui.dialog.select_tracks_to_import.SelectTracksToImportDialogViewState.ReportSelectedTracksState;
import nl.erikduisters.pathfinder.ui.dialog.select_tracks_to_import.SelectTracksToImportDialogViewState.StartTrackSearchState;
import nl.erikduisters.pathfinder.util.menu.MyMenu;
import nl.erikduisters.pathfinder.util.menu.MyMenuItem;
import timber.log.Timber;

/**
 * Created by Erik Duisters on 13-08-2018.
 */

//TODO: Implement showing track preview on map in a dialog or popup window
//TODO: Add select all/none
//TODO: Sort markers on distance
@Singleton
public class SelectTracksToImportDialogViewModel
        extends ViewModel
        implements SelectedMarkersProvider.OnSelectionChangedListener {
    private final MutableLiveData<SelectTracksToImportDialogViewState> viewStateObservable;
    private final MutableLiveData<DisplayShortMessageState> displayShortMessageObservable;
    private Message messageToDisplay;
    @Nullable SelectedMarkersProvider selectedMarkersProvider;

    @Inject
    SelectTracksToImportDialogViewModel() {
        messageToDisplay = null;
        viewStateObservable = new MutableLiveData<>();
        displayShortMessageObservable = new MutableLiveData<>();
    }

    LiveData<SelectTracksToImportDialogViewState> getViewStateObservable() {
        return viewStateObservable;
    }

    LiveData<DisplayShortMessageState> getDisplayShortMessageObservable() {
        return displayShortMessageObservable;
    }

    void setSelectedMarkerProvider(@Nullable SelectedMarkersProvider provider) {
        if (selectedMarkersProvider != null) {
            selectedMarkersProvider.setOnSelectionChangedListener(null);
        }

        if (provider != null) {
            provider.setOnSelectionChangedListener(this);
        }

        this.selectedMarkersProvider = provider;
    }

    void set(SearchTracks.JobInfo jobInfo) {
        if (messageToDisplay != null) {
            viewStateObservable.setValue(new DisplayMessageState(createOptionsMenu(), jobInfo, messageToDisplay));
            messageToDisplay = null;
        } else if (viewStateObservable.getValue() == null) {
            viewStateObservable.setValue(new StartTrackSearchState(createOptionsMenu(), jobInfo, R.string.searching_for_tracks));
        }
    }

    private MyMenu createOptionsMenu() {
        MyMenu optionsMenu = new MyMenu();
        optionsMenu.add(new MyMenuItem(R.id.menu_import, false, true));
        optionsMenu.add(new MyMenuItem(R.id.menu_search, false, false));

        return optionsMenu;
    }

    private SelectTracksToImportDialogViewState getCurrentViewState() {
        SelectTracksToImportDialogViewState currentState = viewStateObservable.getValue();

        if (currentState == null) {
            throw new IllegalStateException("Not expecting current view state to be null");
        }

        return currentState;
    }

    private DataState getCurrentDataState() {
        SelectTracksToImportDialogViewState currentState = getCurrentViewState();

        if (currentState instanceof DataState) {
            return (DataState) currentState;
        } else {
            throw new IllegalStateException("Current view state is not DataState");
        }
    }

    void onResult(Result result) {
        SelectTracksToImportDialogViewState currentState = getCurrentViewState();

        if (result instanceof Result.Error) {
            if (currentState instanceof StartTrackSearchState) {
                Result.Error error = (Result.Error) result;
                StartTrackSearchState state = (StartTrackSearchState) currentState;

                Message msg = new Message(error.exception == null, error.messageResId, error.formatArgs);
                viewStateObservable.setValue(new DisplayMessageState(state.optionsMenu, state.jobInfo, msg));

                if (error.exception != null) {
                    Crashlytics.logException(error.exception);
                }
            }
        }

        if (result instanceof Result.NoNetworAvailableError) {
            if (currentState instanceof StartTrackSearchState) {
                StartTrackSearchState state = (StartTrackSearchState) currentState;

                Message msg = new Message(true, R.string.no_network_available);
                viewStateObservable.setValue(new DisplayMessageState(state.optionsMenu, state.jobInfo, msg));
            }
        }

        if (result instanceof Result.SearchResult) {
            StartTrackSearchState startTrackSearchState = (StartTrackSearchState) currentState;

            Result.SearchResult searchResult = (Result.SearchResult) result;

            viewStateObservable.setValue(new DataState(startTrackSearchState.optionsMenu, searchResult.markers, R.string.no_tracks_found));

            if (searchResult.maxReached) {
                displayShortMessageObservable.setValue(
                        new DisplayShortMessageState(DisplayDuration.LONG, R.string.max_search_results_reached, searchResult.markers.size()));
            }
        }
    }

    void onShortMessageDisplayed() {
        displayShortMessageObservable.setValue(null);
    }

    void onRetryClicked() {
        DisplayMessageState currentState = (DisplayMessageState) getCurrentViewState();

        viewStateObservable.setValue(new StartTrackSearchState(currentState.optionsMenu, currentState.jobInfo, R.string.searching_for_tracks));
    }

    @Override
    public void onSelectionChanged(int numSelected) {
        DataState currentState = getCurrentDataState();

        if (currentState.optionsMenu.findItem(R.id.menu_import).isEnabled() != numSelected > 0) {
            MyMenu optionsMenu = new MyMenu(currentState.optionsMenu);
            optionsMenu.findItem(R.id.menu_import).setEnabled(numSelected > 0);

            viewStateObservable.setValue(new DataState(optionsMenu, currentState.markers, currentState.emptyListMessage));
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        viewStateObservable.setValue(null);
    }

    void onMenuItemSelected(MyMenuItem menuItem) {
        switch (menuItem.getId()) {
            case R.id.menu_import:
                if (selectedMarkersProvider != null) {
                    List<String> selectedTrackFileIds = getSelectedTrackFileIds(selectedMarkersProvider.getSelectedMarkers());

                    viewStateObservable.setValue(new ReportSelectedTracksState(selectedTrackFileIds));
                } else {
                    throw new IllegalStateException("Import clicked but SelectedMarkersProvider is not set");
                }
                break;
        }
    }

    private List<String> getSelectedTrackFileIds(List<Marker> selectedMarkers) {
        List<String> selectedTrackFileIds = new ArrayList<>(selectedMarkers.size());

        for (Marker marker : selectedMarkers) {
            selectedTrackFileIds.add(marker.fileId);
        }

        return selectedTrackFileIds;
    }

    void onSelectedMarkersReported() {
        viewStateObservable.setValue(null);
    }

    //TODO: Saving 250 markers to a Parcelable could become problematic. Maybe save them to preferences (as a json string) or room
    Parcelable onSaveInstanceState() {
        SelectTracksToImportDialogViewState currentState = getCurrentViewState();

        SavedState savedState = null;

        if (currentState instanceof DataState) {
            savedState = new SavedState();
            savedState.markers = ((DataState) currentState).markers;
            savedState.message = null;
        } else if (currentState instanceof DisplayMessageState) {
            DisplayMessageState displayMessageState = (DisplayMessageState) currentState;

            savedState = new SavedState();
            savedState.markers = null;
            savedState.message = displayMessageState.message;
        }

        return savedState;
    }

    void onRestoreInstanceState(@Nullable Parcelable state) {
        if (state == null || viewStateObservable.getValue() != null) {
            Timber.d("onRestoreInstanceState: Not restoring state");
            return;
        }

        SavedState savedState = (SavedState) state;

        if (savedState.markers != null) {
            viewStateObservable.setValue(new DataState(createOptionsMenu(), savedState.markers, R.string.no_tracks_found));
        } else {
            messageToDisplay = savedState.message;
        }
    }

    private static class SavedState implements Parcelable {
        @Nullable List<Marker> markers;
        @Nullable Message message;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeTypedList(this.markers);
            dest.writeParcelable(this.message, flags);
        }

        public SavedState() {
        }

        protected SavedState(Parcel in) {
            this.markers = in.createTypedArrayList(Marker.CREATOR);
            this.message = in.readParcelable(Message.class.getClassLoader());
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

    static class Message implements Parcelable {
        final @StringRes int message;
        @Nullable final Object[] formatArgs;
        final boolean isRetryable;

        Message(boolean isRetryable, @StringRes int message, @Nullable Object... formatArgs) {
            this.isRetryable = isRetryable;
            this.message = message;
            this.formatArgs = formatArgs;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.message);
            dest.writeArray(this.formatArgs);
            dest.writeByte(this.isRetryable ? (byte) 1 : (byte) 0);
        }

        protected Message(Parcel in) {
            this.message = in.readInt();
            this.formatArgs = in.readArray(getClass().getClassLoader());
            this.isRetryable = in.readByte() != 0;
        }

        public static final Creator<Message> CREATOR = new Creator<Message>() {
            @Override
            public Message createFromParcel(Parcel source) {
                return new Message(source);
            }

            @Override
            public Message[] newArray(int size) {
                return new Message[size];
            }
        };
    }
}