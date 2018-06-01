package nl.erikduisters.pathfinder;

import android.app.Application;
import android.support.annotation.NonNull;

import com.crashlytics.android.Crashlytics;
import com.squareup.leakcanary.LeakCanary;

import io.fabric.sdk.android.Fabric;
import timber.log.Timber;

/**
 * Created by Erik Duisters on 01-06-2018.
 */

//TODO: Before submission create a readme file that explains that the reviewer needs to provide his/her own google-services.json file downloadable from the firebase console
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        if (LeakCanary.isInAnalyzerProcess(this)) {
            return;
        }

        LeakCanary.install(this);

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Timber.plant(new ReleaseTree());
        }

        //TODO: Maybe make this optional through a preference
        Fabric.with(this, new Crashlytics());
    }

    private static class ReleaseTree extends Timber.Tree {
        @Override
        protected void log(int priority, String tag, @NonNull String message, Throwable t) {
        }
    }
}
