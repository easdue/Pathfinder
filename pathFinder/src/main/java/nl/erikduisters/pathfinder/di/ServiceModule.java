package nl.erikduisters.pathfinder.di;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import nl.erikduisters.pathfinder.service.MapDownloadService;
import nl.erikduisters.pathfinder.service.gpsies_service.GPSiesService;
import nl.erikduisters.pathfinder.service.track_import.TrackImportService;

/**
 * Created by Erik Duisters on 28-07-2018.
 */

@Module
public abstract class ServiceModule {
    @ContributesAndroidInjector
    abstract MapDownloadService bindDownloadService();

    @ContributesAndroidInjector
    abstract GPSiesService bindGPSiesService();

    @ContributesAndroidInjector
    abstract TrackImportService bindTrackImportService();
}
