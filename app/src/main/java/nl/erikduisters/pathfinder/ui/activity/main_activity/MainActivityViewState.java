package nl.erikduisters.pathfinder.ui.activity.main_activity;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import nl.erikduisters.pathfinder.ui.dialog.MessageWithTitle;

/**
 * Created by Erik Duisters on 03-06-2018.
 */
interface MainActivityViewState {
    class InitStorageViewState implements MainActivityViewState {
    }

    class InitDatabaseState implements MainActivityViewState {
    }

    class RequestRuntimePermissionState implements MainActivityViewState {
    }

    class CheckGoogleApiUnavailabilityState implements MainActivityViewState {
    }

    class ShowMessageState implements MainActivityViewState {
        @NonNull final MessageWithTitle message;
        final boolean isFatal;
        @Nullable MainActivityViewState prevState;

        ShowMessageState(@NonNull MessageWithTitle message, boolean isFatal) {
            this(message, isFatal, null);
        }

        ShowMessageState(@NonNull MessageWithTitle message, boolean isFatal, @Nullable MainActivityViewState prevState) {
            this.message = message;
            this.isFatal = isFatal;
            this.prevState = prevState;
        }
    }

    class FinishState implements MainActivityViewState {
    }
}
