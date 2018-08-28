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

package nl.erikduisters.pathfinder.ui.dialog.import_settings;

import android.support.annotation.NonNull;

import java.io.File;
import java.util.List;

import nl.erikduisters.pathfinder.service.gpsies_service.SearchTracks;
import nl.erikduisters.pathfinder.ui.dialog.MessageWithTitle;
import nl.erikduisters.pathfinder.util.menu.MyMenu;

/**
 * Created by Erik Duisters on 08-08-2018.
 */
public interface ImportSettingsDialogViewState {
    class InitializedState implements ImportSettingsDialogViewState {
        @NonNull final MyMenu optionsMenu;
        @NonNull final ImportSettingsAdapterData importSettingsAdapterData;

        InitializedState(@NonNull MyMenu optionsMenu, @NonNull ImportSettingsAdapterData importSettingsAdapterData) {
            this.optionsMenu = optionsMenu;
            this.importSettingsAdapterData = importSettingsAdapterData;
        }
    }

    class ShowCancelMessageDialogState extends InitializedState {
        @NonNull public final MessageWithTitle messageWithTitle;

        ShowCancelMessageDialogState(InitializedState initializedState, @NonNull MessageWithTitle messageWithTitle) {
            super(initializedState.optionsMenu, initializedState.importSettingsAdapterData);

            this.messageWithTitle = messageWithTitle;
        }
    }

    interface DismissDialogState extends ImportSettingsDialogViewState {
        final class ReportSearchTracksState implements DismissDialogState {
            final @NonNull SearchTracks.JobInfo jobInfo;

            ReportSearchTracksState(@NonNull SearchTracks.JobInfo jobInfo) {
                this.jobInfo = jobInfo;
            }
        }

        final class ReportImportFilesState implements DismissDialogState {
            final @NonNull List<File> filesToImport;

            ReportImportFilesState(@NonNull List<File> filesToImport) {
                this.filesToImport = filesToImport;
            }
        }
    }
}

