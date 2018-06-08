package nl.erikduisters.pathfinder.di;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import nl.erikduisters.pathfinder.ui.activity.main_activity.MainActivity;
import nl.erikduisters.pathfinder.ui.activity.main_activity.MainActivityBindingModule;

/**
 * Created by Erik Duisters on 01-06-2018.
 */

@Module
abstract class ActivityBindingModule {
    @ContributesAndroidInjector(modules = MainActivityBindingModule.class)
    abstract MainActivity contributeMainActivity();
}
