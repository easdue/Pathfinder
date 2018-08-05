package nl.erikduisters.pathfinder.ui.activity.map_download;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import nl.erikduisters.pathfinder.di.FragmentScope;
import nl.erikduisters.pathfinder.ui.fragment.map_download.MapDownloadFragment;

/**
 * Created by Erik Duisters on 26-07-2018.
 */
@Module
public abstract class MapDownloadActivityBindingModule {
    @FragmentScope
    @ContributesAndroidInjector
    abstract MapDownloadFragment contributeMapDownloadFragmentInjector();
}
