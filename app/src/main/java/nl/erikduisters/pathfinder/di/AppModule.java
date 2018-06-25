package nl.erikduisters.pathfinder.di;

import android.app.Application;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Looper;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import nl.erikduisters.pathfinder.MyApplication;
import nl.erikduisters.pathfinder.data.local.database.PathfinderDatabase;
import nl.erikduisters.pathfinder.util.MainThreadExecutor;

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
                .addMigrations(PathfinderDatabase.getMigrations())
                .build();
    }

    @Provides
    @Singleton
    @Named("MainLooper")
    static Handler provideHandler() {
        return new Handler(Looper.getMainLooper());
    }

    @Provides
    @Singleton
    static MainThreadExecutor provideMainThreadExecutor() {
        return new MainThreadExecutor();
    }

    @Provides
    @Singleton
    static LocationManager provideLocationManager(@ApplicationContext Context context) {
        return (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    @Provides
    @Singleton
    static FusedLocationProviderClient providedFusedLocationProviderClient(@ApplicationContext Context context) {
        return LocationServices.getFusedLocationProviderClient(context);
    }
}
