package nl.erikduisters.pathfinder.ui.dialog.select_tracks_to_import;

import android.content.Context;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

import javax.annotation.Nullable;

import nl.erikduisters.pathfinder.data.model.Marker;
import nl.erikduisters.pathfinder.service.gpsies_service.SearchTracks;
import nl.erikduisters.pathfinder.ui.dialog.select_tracks_to_import.SelectTracksToImportDialogViewModel.Message;
import nl.erikduisters.pathfinder.util.menu.MyMenu;

/**
 * Created by Erik Duisters on 13-08-2018.
 */
public interface SelectTracksToImportDialogViewState {
    class ShowProgressState implements SelectTracksToImportDialogViewState {
        final @NonNull MyMenu optionsMenu;
        final private @StringRes int messageResId;

        ShowProgressState(@NonNull MyMenu optionsMenu, @StringRes int messageResId) {
            this.optionsMenu = optionsMenu;
            this.messageResId = messageResId;
        }

        public String getMessage(Context context) {
            return context.getString(messageResId);
        }
    }

    class StartTrackSearchState extends ShowProgressState {
        final @NonNull SearchTracks.JobInfo jobInfo;

        StartTrackSearchState(@NonNull MyMenu optionsMenu, @NonNull SearchTracks.JobInfo jobInfo, @StringRes int progressMessageResId) {
            super(optionsMenu, progressMessageResId);

            this.jobInfo = jobInfo;
        }
    }

    class DataState implements SelectTracksToImportDialogViewState {
        final @NonNull MyMenu optionsMenu;
        final @NonNull List<Marker> markers;
        final @StringRes int emptyListMessage;

        DataState(@NonNull MyMenu optionsMenu, @NonNull List<Marker> markers, @StringRes int emptyListMessage) {
            this.optionsMenu = optionsMenu;
            this.markers = markers;
            this.emptyListMessage = emptyListMessage;
        }
    }

    class DisplayMessageState extends ShowProgressState {
        final @NonNull SearchTracks.JobInfo jobInfo;
        final Message message;

        DisplayMessageState(@NonNull MyMenu optionsMenu, @NonNull SearchTracks.JobInfo jobInfo, Message message) {
            super(optionsMenu, 0);

            this.jobInfo = jobInfo;
            this.message = message;
        }

        @Override
        public String getMessage(Context context) {
            return context.getString(message.message, message.formatArgs);
        }

        public boolean isRetryable() { return message.isRetryable; }
    }

    class ReportSelectedTracksState implements SelectTracksToImportDialogViewState {
        final @NonNull List<String> selectedTrackFileIds;

        ReportSelectedTracksState(@NonNull List<String> selectedTrackFileIds) {
            this.selectedTrackFileIds = selectedTrackFileIds;
        }
    }

    class DisplayShortMessageState {
        @IntDef({DisplayDuration.SHORT, DisplayDuration.LONG})
        @Retention(RetentionPolicy.SOURCE)
        @interface DisplayDuration {
            int SHORT = Snackbar.LENGTH_SHORT;
            int LONG = Snackbar.LENGTH_LONG;
        }

        private final @StringRes int messageResId;
        private final @Nullable Object[] args;
        final @DisplayDuration int displayDuration;

        DisplayShortMessageState(@DisplayDuration int displayDuration, @StringRes int messageResId, @Nullable Object... args) {
            this.displayDuration = displayDuration;
            this.messageResId = messageResId;
            this.args = args;
        }

        String getMessage(Context context) {
            if (args == null) {
                return context.getString(messageResId);
            } else {
                return context.getString(messageResId, (Object[]) args);
            }
        }
    }
}
