package nl.erikduisters.pathfinder.viewmodel;

import javax.inject.Inject;
import javax.inject.Singleton;

import nl.erikduisters.pathfinder.data.local.PreferenceManager;
import nl.erikduisters.pathfinder.ui.BaseActivityViewModel;

/**
 * Created by Erik Duisters on 30-07-2018.
 */

@Singleton
public class VoidViewModel extends BaseActivityViewModel {
    @Inject
    VoidViewModel(PreferenceManager preferenceManager) {
        super(preferenceManager);
    }
}
