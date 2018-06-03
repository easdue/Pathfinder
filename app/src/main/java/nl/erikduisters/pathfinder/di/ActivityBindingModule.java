package nl.erikduisters.pathfinder.di;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import nl.erikduisters.pathfinder.ui.activity.main_activity.MainActivity;

/**
 * Created by Erik Duisters on 01-06-2018.
 */

@Module
abstract class ActivityBindingModule {
    @ActivityScope
    @ContributesAndroidInjector(/*modules = MainActivityModule.class*/)
    abstract MainActivity mainActivity();
}
