package nl.erikduisters.pathfinder.ui.activity.main_activity;

import android.arch.lifecycle.ViewModel;

import javax.inject.Inject;
import javax.inject.Singleton;

import timber.log.Timber;

/**
 * Created by Erik Duisters on 03-06-2018.
 */
@Singleton
public class MainActivityViewModel extends ViewModel {
    @Inject
    MainActivityViewModel() {
        Timber.d("New MainActivityViewModel created");
    }
}
