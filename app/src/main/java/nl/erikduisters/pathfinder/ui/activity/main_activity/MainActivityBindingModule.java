package nl.erikduisters.pathfinder.ui.activity.main_activity;

import android.content.Context;

import dagger.Module;
import dagger.Provides;
import dagger.android.ContributesAndroidInjector;
import nl.erikduisters.pathfinder.di.ActivityContext;
import nl.erikduisters.pathfinder.di.FragmentScope;
import nl.erikduisters.pathfinder.ui.fragment.init_storage.InitStorageFragment;

/**
 * Created by Erik Duisters on 05-06-2018.
 */
@Module
public abstract class MainActivityBindingModule {
    @FragmentScope
    @ContributesAndroidInjector
    abstract InitStorageFragment contributeInitStorageFragment();

    @Provides
    @ActivityContext
    static Context provideActivityContext(MainActivity activity) {
        return activity;
    }
}
