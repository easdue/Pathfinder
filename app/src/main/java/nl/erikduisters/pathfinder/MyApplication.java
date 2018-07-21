package nl.erikduisters.pathfinder;

import android.app.Activity;
import android.app.Application;
import android.os.Build;
import android.support.annotation.NonNull;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.facebook.stetho.Stetho;
import com.squareup.leakcanary.LeakCanary;

import javax.inject.Inject;

import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;
import io.fabric.sdk.android.Fabric;
import nl.erikduisters.pathfinder.data.local.SvgRenderer;
import nl.erikduisters.pathfinder.di.DaggerAppComponent;
import nl.erikduisters.pathfinder.ui.widget.SvgView;
import timber.log.Timber;

/**
 * Created by Erik Duisters on 01-06-2018.
 */

//TODO: Before submission create a readme file that explains that the reviewer needs to provide his/her own google-services.json file downloadable from the firebase console
public class MyApplication extends Application implements HasActivityInjector {
    @Inject
    DispatchingAndroidInjector<Activity> dispatchingAndroidInjector;
    @Inject
    SvgRenderer svgRenderer;

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

        //TODO: Maybe make this optional through a preference see: https://docs.fabric.io/android/crashlytics/build-tools.html
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
    }

    private static class ReleaseTree extends Timber.Tree {
        @Override
        protected void log(int priority, String tag, @NonNull String message, Throwable t) {
        }
    }

    @Override
    public DispatchingAndroidInjector<Activity> activityInjector() {
        return dispatchingAndroidInjector;
    }
}
