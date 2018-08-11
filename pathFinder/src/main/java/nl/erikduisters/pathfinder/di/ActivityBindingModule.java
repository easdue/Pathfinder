package nl.erikduisters.pathfinder.di;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import nl.erikduisters.pathfinder.ui.activity.gps_status.GpsStatusActivity;
import nl.erikduisters.pathfinder.ui.activity.gps_status.GpsStatusActivityBindingModule;
import nl.erikduisters.pathfinder.ui.activity.main_activity.MainActivity;
import nl.erikduisters.pathfinder.ui.activity.main_activity.MainActivityBindingModule;
import nl.erikduisters.pathfinder.ui.activity.map_download.MapDownloadActivity;
import nl.erikduisters.pathfinder.ui.activity.map_download.MapDownloadActivityBindingModule;
import nl.erikduisters.pathfinder.ui.activity.settings.SettingsActivity;
import nl.erikduisters.pathfinder.ui.activity.settings.SettingsActivityBindingModule;

/**
 * Created by Erik Duisters on 01-06-2018.
 */

@Module
abstract class ActivityBindingModule {
    @ContributesAndroidInjector(modules = MainActivityBindingModule.class)
    abstract MainActivity contributeMainActivity();

    @ContributesAndroidInjector(modules = GpsStatusActivityBindingModule.class)
    abstract GpsStatusActivity contributeGpsStatusActivity();

    @ContributesAndroidInjector(modules = SettingsActivityBindingModule.class)
    abstract SettingsActivity contributeSettingsActivity();

    @ContributesAndroidInjector(modules = MapDownloadActivityBindingModule.class)
    abstract MapDownloadActivity contributeMapDownloadActivity();
}
