package nl.erikduisters.pathfinder.di;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import nl.erikduisters.pathfinder.service.MapDownloadService;

/**
 * Created by Erik Duisters on 28-07-2018.
 */

@Module
public abstract class ServiceModule {
    @ContributesAndroidInjector
    abstract MapDownloadService bindMapUnzipService();
}
