package nl.erikduisters.pathfinder.ui.fragment.init_storage;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

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

    final class StorageInitializedState implements InitStorageFragmentViewState {
        boolean initializationSuccessfull;
        @Nullable final MessageWithTitle failureMessage;
        final boolean isFatal;

        private StorageInitializedState(boolean initializationSuccessfull, @Nullable MessageWithTitle failureMessage, boolean isFatal) {
            this.initializationSuccessfull = initializationSuccessfull;
            this.failureMessage = failureMessage;
            this.isFatal = isFatal;
        }

        static StorageInitializedState getSuccessState() {
            return new StorageInitializedState(true, null, false);
        }

        static StorageInitializedState getFailedState(@NonNull MessageWithTitle failureMessage, boolean isFatal) {
            return new StorageInitializedState(false, failureMessage, isFatal);
        }
    }
}
