package nl.erikduisters.pathfinder.ui.fragment.init_storage;

import android.support.annotation.NonNull;

import java.util.ArrayList;

import nl.erikduisters.pathfinder.ui.dialog.MessageWithTitle;
import nl.erikduisters.pathfinder.ui.dialog.PositiveNegativeButtonMessageDialog;

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

    final class ShowFatalMessageDialogState implements InitStorageFragmentViewState {
        @NonNull final MessageWithTitle message;

        ShowFatalMessageDialogState(@NonNull MessageWithTitle messageWithTitle) {
            this.message = messageWithTitle;
        }
    }

    final class ShowPositiveNegativeButtonMessageDialogState implements InitStorageFragmentViewState {
        @NonNull final PositiveNegativeButtonMessageDialog.DialogInfo dialogInfo;

        ShowPositiveNegativeButtonMessageDialogState(@NonNull PositiveNegativeButtonMessageDialog.DialogInfo dialogInfo) {
            this.dialogInfo = dialogInfo;
        }
    }

    final class StorageInitializedState implements InitStorageFragmentViewState {}

    final class StorageInitializationFailedState implements InitStorageFragmentViewState {}
}
