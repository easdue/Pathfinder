package nl.erikduisters.pathfinder;

import android.app.Activity;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.os.Build;
import android.support.annotation.NonNull;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.facebook.stetho.Stetho;
import com.squareup.leakcanary.LeakCanary;

import javax.inject.Inject;

import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;
import dagger.android.HasServiceInjector;
import io.fabric.sdk.android.Fabric;
import nl.erikduisters.pathfinder.data.local.PreferenceManager;
import nl.erikduisters.pathfinder.data.local.SvgRenderer;
import nl.erikduisters.pathfinder.di.DaggerAppComponent;
import nl.erikduisters.pathfinder.ui.widget.SvgView;
import nl.erikduisters.pathfinder.util.NotificationChannels;
import timber.log.Timber;

/**
 * Created by Erik Duisters on 01-06-2018.
 */

//TODO: Before submission create a readme file that explains that the reviewer needs to provide his/her own google-services.json file downloadable from the firebase console
public class MyApplication extends Application implements HasActivityInjector, HasServiceInjector {
    @Inject
    DispatchingAndroidInjector<Activity> dispatchingAndroidActivityInjector;
    @Inject
    DispatchingAndroidInjector<Service> dispatchingAndroidServiceInjector;
    @Inject
    SvgRenderer svgRenderer;
    @Inject
    PreferenceManager preferenceManager;    //Make sure Coordinate, Distance and Speed get initialized

    @Override
    public void onCreate() {
        super.onCreate();

        if (LeakCanary.isInAnalyzerProcess(this)) {
            return;
        }

        LeakCanary.install(this);

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
            Stetho.initializeWithDefaults(this);
        } else {
            Timber.plant(new ReleaseTree());
        }

        //TODO: Maybe make this optional through a preference
        //TODO: I have excluded firebase-core (Analytics) in build.gradle because if Crashlytics is disabled here it crashed my app on at least API 19 due to "too many open files"
        if (Build.PRODUCT.startsWith("sdk") || Build.PRODUCT.startsWith("vbox")) {
            Timber.d("Disabled Crashlytics");
            CrashlyticsCore disabled = new CrashlyticsCore.Builder()
                    .disabled(true)
                    .build();

            Fabric.with(this, new Crashlytics.Builder().core(disabled).build());
        } else {
            Timber.d("Enabled Crashlytics");
            Fabric.with(this, new Crashlytics());
        }

        DaggerAppComponent.builder()
                .create(this)
                .inject(this);

        SvgView.init(svgRenderer);

        createNotificationChannels();
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT < 26) {
            return;
        }

        NotificationManager notificationManager = getSystemService(NotificationManager.class);

        if (notificationManager == null) {
            return;
        }

        for (NotificationChannels myChannel : NotificationChannels.values()) {
            CharSequence name = getString(myChannel.getChannelNameResId());
            String description = getString(myChannel.getChannelDescriptionResId());

            NotificationChannel channel = new NotificationChannel(myChannel.getChannelId(), name, myChannel.getImportance());
            channel.setDescription(description);

            notificationManager.createNotificationChannel(channel);
        }
    }

    private static class ReleaseTree extends Timber.Tree {
        @Override
        protected void log(int priority, String tag, @NonNull String message, Throwable t) {
        }
    }

    @Override
    public DispatchingAndroidInjector<Activity> activityInjector() {
        return dispatchingAndroidActivityInjector;
    }

    @Override
    public DispatchingAndroidInjector<Service> serviceInjector() {
        return dispatchingAndroidServiceInjector;
    }
}
