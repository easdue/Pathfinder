/*
 * Copyright (c) 2018 Erik Duisters
 *
 * This file is part of Pathfinder
 *
 * Pathfinder is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Foobar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Pathfinder. If not, see <https://www.gnu.org/licenses/>.
 */

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

import java.util.concurrent.TimeUnit;

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
        //TODO: Use PreferenceManager.getStorageDir() after MainActivityViewModel switches InitStorage and InitDatabase
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
    static OkHttpClient provideOkHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        if (BuildConfig.DEBUG) {
            builder.addNetworkInterceptor(new StethoInterceptor());
        }

        return builder.build();
    }

    /*
       TODO: Setup cookieJar: The height chart is generated with metric/imperial scale depending on cookies u=imperial|metric
             As soon as I am able to login the cookie is also used to identify me

             CookieJar cookieJar = new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(context));

             OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .cookieJar(cookieJar)
                    .build();
    */
    @Provides
    @Singleton
    @GPSiesOkHttpClient
    static OkHttpClient provideGPSiesOkHttpClient(OkHttpClient okHttpClient) {
        return okHttpClient.newBuilder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    @Provides
    @Singleton
    static AssetManager provideAssetManager(@ApplicationContext Context context) {
        return context.getAssets();
    }
}
