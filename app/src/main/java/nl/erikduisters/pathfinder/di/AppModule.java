package nl.erikduisters.pathfinder.di;

import android.app.Application;
import android.content.Context;

import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import nl.erikduisters.pathfinder.MyApplication;

/**
 * Created by Erik Duisters on 01-06-2018.
 */

@Module
abstract class AppModule {
    @Binds
    @Singleton
    abstract Application application(MyApplication myApplication);

    @Provides
    @ApplicationContext
    static Context provideApplicationContext(Application application) {
        return application.getApplicationContext();
    }
}
