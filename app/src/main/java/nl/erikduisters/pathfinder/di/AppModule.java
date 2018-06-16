package nl.erikduisters.pathfinder.di;

import android.app.Application;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import nl.erikduisters.pathfinder.MyApplication;
import nl.erikduisters.pathfinder.data.local.database.PathfinderDatabase;

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

    @Provides
    @Singleton
    static PathfinderDatabase providePathfinderDatabase(@ApplicationContext Context context) {
        return Room.databaseBuilder(context, PathfinderDatabase.class, "pathfinder.db")
                .build();
    }

    @Provides
    @Singleton
    @Named("MainLooper")
    static Handler provideHandler() {
        return new Handler(Looper.getMainLooper());
    }
}
