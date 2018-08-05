package nl.erikduisters.pathfinder.ui.fragment.init_storage;

import android.support.annotation.NonNull;

import java.util.ArrayList;

import nl.erikduisters.pathfinder.ui.dialog.MessageWithTitle;

/**
 * Created by Erik Duisters on 03-06-2018.
 */
interface InitStorageFragmentViewState {
    final class SelectStorageState implements InitStorageFragmentViewState {
        final ArrayList<Storage> storageList;

        SelectStorageState(ArrayList<Storage> storageList) {
            this.storageList = storageList;
        }
    }

    final class ShowSpaceRequirementWarningState implements InitStorageFragmentViewState {
        @NonNull final MessageWithTitle message;

        ShowSpaceRequirementWarningState(@NonNull MessageWithTitle message) {
            this.message = message;
        }
    }

    final class StorageInitializedState implements InitStorageFragmentViewState {}

    final class StorageInitializationFailedState implements InitStorageFragmentViewState {
        @NonNull final MessageWithTitle failureMessage;
        final boolean isFatal;

        StorageInitializationFailedState(@NonNull MessageWithTitle failureMessage, boolean isFatal) {
            this.failureMessage = failureMessage;
            this.isFatal = isFatal;
        }
    }
}
