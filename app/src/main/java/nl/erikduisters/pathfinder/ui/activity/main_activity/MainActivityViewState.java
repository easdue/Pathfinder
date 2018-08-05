package nl.erikduisters.pathfinder.ui.activity.main_activity;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import nl.erikduisters.pathfinder.data.usecase.InitDatabase;
import nl.erikduisters.pathfinder.ui.dialog.MessageWithTitle;
import nl.erikduisters.pathfinder.ui.dialog.ProgressDialog;
import nl.erikduisters.pathfinder.ui.fragment.runtime_permission.RuntimePermissionRequest;

/**
 * Created by Erik Duisters on 03-06-2018.
 */
interface MainActivityViewState {
    class InitDatabaseState implements MainActivityViewState {
        @NonNull final ProgressDialog.Properties dialogProperties;
        @Nullable final InitDatabase.Progress progress;

        InitDatabaseState(@NonNull ProgressDialog.Properties dialogProperties, @Nullable InitDatabase.Progress progress) {
            this.dialogProperties = dialogProperties;
            this.progress = progress;
        }

        InitDatabaseState createNewWithUpdateProgress(@NonNull InitDatabase.Progress progress) {
            return new InitDatabaseState(this.dialogProperties, progress);
        }
    }

    class InitStorageViewState implements MainActivityViewState {}

    class RequestRuntimePermissionState implements MainActivityViewState {
        final RuntimePermissionRequest request;

        RequestRuntimePermissionState(RuntimePermissionRequest request) {
            this.request = request;
        }
    }

    class CheckPlayServicesAvailabilityState implements MainActivityViewState {
    }

    //TODO: Think of a better way to handle these type of "snackbar" messages. Maybe move to MainActivityViewState
    class ShowMessageState implements MainActivityViewState {
        @NonNull final MessageWithTitle message;
        //TODO: This needs to become nextState;
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

    class AskUserToEnableGpsState implements MainActivityViewState {
        final @NonNull MessageWithTitle message;
        final boolean showNeverAskAgain;
        final @StringRes int positiveButtonTextResId;
        final @StringRes int negativeButtonTextResId;

        AskUserToEnableGpsState(@NonNull MessageWithTitle message, boolean showNeverAskAgain, @StringRes int positiveButtonTextResId, @StringRes int negativeButtonTextResId) {
            this.message = message;
            this.showNeverAskAgain = showNeverAskAgain;
            this.positiveButtonTextResId = positiveButtonTextResId;
            this.negativeButtonTextResId = negativeButtonTextResId;
        }
    }

    class ShowEnableGpsSettingState implements MainActivityViewState {}
    class WaitingForGpsToBeEnabledState implements MainActivityViewState {}

    class InitializedState implements MainActivityViewState { }
}
