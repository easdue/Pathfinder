package nl.erikduisters.pathfinder.di;

import android.app.Application;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Looper;

import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import nl.erikduisters.pathfinder.BuildConfig;
import nl.erikduisters.pathfinder.MyApplication;
import nl.erikduisters.pathfinder.data.local.database.PathfinderDatabase;
import nl.erikduisters.pathfinder.util.MainThreadExecutor;
import okhttp3.OkHttpClient;

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

    @Provides
    @Singleton
    @Named("VersionCode")
    static int provideVersionCode(@ApplicationContext Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException("Hey guys, apparently I'm not installed");
        }
    }

    @Provides
    @Singleton
    static OkHttpClient.Builder provideOkHttpClientBuilder() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        if (BuildConfig.DEBUG) {
            builder.addNetworkInterceptor(new StethoInterceptor());
        }

        return builder;
    }

    @Provides
    @Singleton
    static AssetManager provideAssetManager(@ApplicationContext Context context) {
        return context.getAssets();
    }
}
