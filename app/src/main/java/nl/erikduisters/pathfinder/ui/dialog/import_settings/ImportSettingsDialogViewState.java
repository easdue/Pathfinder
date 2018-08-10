package nl.erikduisters.pathfinder.ui.dialog.import_settings;

import android.support.annotation.NonNull;

import nl.erikduisters.pathfinder.util.menu.MyMenu;

/**
 * Created by Erik Duisters on 08-08-2018.
 */
public class ImportSettingsDialogViewState {
    @NonNull final MyMenu optionsMenu;
    @NonNull final ImportSettingsAdapterData importSettingsAdapterData;

    ImportSettingsDialogViewState(@NonNull MyMenu optionsMenu, @NonNull ImportSettingsAdapterData importSettingsAdapterData) {
        this.optionsMenu = optionsMenu;
        this.importSettingsAdapterData = importSettingsAdapterData;
    }
}
