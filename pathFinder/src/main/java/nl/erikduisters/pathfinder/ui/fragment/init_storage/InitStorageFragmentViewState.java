/*
 * Copyright (c) 2018 Erik Duisters
 *
 * This file is part of Pathfinder
 *
 * Pathfinder is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Foobar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Pathfinder. If not, see <https://www.gnu.org/licenses/>.
 */

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
