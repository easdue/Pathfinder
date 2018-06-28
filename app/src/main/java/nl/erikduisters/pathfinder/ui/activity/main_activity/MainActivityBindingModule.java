package nl.erikduisters.pathfinder.ui.activity.main_activity;

import android.content.Context;

import dagger.Module;
import dagger.Provides;
import dagger.android.ContributesAndroidInjector;
import nl.erikduisters.pathfinder.di.ActivityContext;
import nl.erikduisters.pathfinder.di.FragmentScope;
import nl.erikduisters.pathfinder.ui.fragment.compass.CompassFragment;
import nl.erikduisters.pathfinder.ui.fragment.init_storage.InitStorageFragment;
import nl.erikduisters.pathfinder.ui.fragment.map.MapFragment;
import nl.erikduisters.pathfinder.ui.fragment.play_services.PlayServicesFragment;
import nl.erikduisters.pathfinder.ui.fragment.runtime_permission.RuntimePermissionFragment;
import nl.erikduisters.pathfinder.ui.fragment.track_list.TrackListFragment;

/**
 * Created by Erik Duisters on 05-06-2018.
 */
@Module
public abstract class MainActivityBindingModule {
    @FragmentScope
    @ContributesAndroidInjector
    abstract InitStorageFragment contributeInitStorageFragmentInjector();

    @FragmentScope
    @ContributesAndroidInjector
    abstract RuntimePermissionFragment contributeRuntimePermissionFragmentInjector();

    @FragmentScope
    @ContributesAndroidInjector
    abstract PlayServicesFragment contributePlayServicesAvailabilityFragmentInjector();

    @FragmentScope
    @ContributesAndroidInjector
    abstract TrackListFragment contributeTrackListFragmentInjector();

    @FragmentScope
    @ContributesAndroidInjector
    abstract MapFragment contributeMapFragmentInjector();

    @FragmentScope
    @ContributesAndroidInjector
    abstract CompassFragment contributeCompassFragmentInjector();

    @Provides
    @ActivityContext
    static Context provideActivityContext(MainActivity activity) {
        return activity;
    }
}
