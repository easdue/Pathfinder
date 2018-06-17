package nl.erikduisters.pathfinder.ui.activity.main_activity;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import nl.erikduisters.pathfinder.data.usecase.InitDatabase;
import nl.erikduisters.pathfinder.ui.dialog.MessageWithTitle;
import nl.erikduisters.pathfinder.ui.fragment.runtime_permission.RuntimePermissionRequest;

/**
 * Created by Erik Duisters on 03-06-2018.
 */
interface MainActivityViewState {
    class InitDatabaseState implements MainActivityViewState {
        final int titleResId;
        @Nullable final InitDatabase.Progress progress;

        InitDatabaseState(@StringRes int titleResId, @Nullable InitDatabase.Progress progress) {
            this.titleResId = titleResId;
            this.progress = progress;
        }
    }

    class InitStorageViewState implements MainActivityViewState {
    }

    class RequestRuntimePermissionState implements MainActivityViewState {
        final RuntimePermissionRequest request;

        RequestRuntimePermissionState(RuntimePermissionRequest request) {
            this.request = request;
        }
    }

    class CheckGoogleApiUnavailabilityState implements MainActivityViewState {
    }

    class ShowMessageState implements MainActivityViewState {
        @NonNull final MessageWithTitle message;
        @Nullable MainActivityViewState prevState;

        ShowMessageState(@NonNull MessageWithTitle message, @Nullable MainActivityViewState prevState) {
            this.message = message;
            this.prevState = prevState;
        }
    }

    class ShowFatalErrorMessageState implements MainActivityViewState {
        @NonNull final MessageWithTitle message;
        @Nullable Throwable throwable;

        ShowFatalErrorMessageState(@NonNull MessageWithTitle message, @Nullable Throwable throwable) {
            this.message = message;
            this.throwable = throwable;
        }
    }

    class FinishState implements MainActivityViewState {
    }
}
