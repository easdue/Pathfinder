package nl.erikduisters.pathfinder.ui.activity.map_download;

import javax.inject.Inject;
import javax.inject.Singleton;

import nl.erikduisters.pathfinder.data.local.PreferenceManager;
import nl.erikduisters.pathfinder.ui.BaseActivityViewModel;

/**
 * Created by Erik Duisters on 26-07-2018.
 */

@Singleton
public class MapDownloadActivityViewModel extends BaseActivityViewModel {
    @Inject
    MapDownloadActivityViewModel(PreferenceManager preferenceManager) {
        super(preferenceManager);
    }
}
