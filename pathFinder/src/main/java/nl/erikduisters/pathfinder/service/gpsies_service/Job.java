package nl.erikduisters.pathfinder.service.gpsies_service;

import android.os.Parcelable;
import android.support.annotation.NonNull;

import okhttp3.OkHttpClient;

/**
 * Created by Erik Duisters on 12-08-2018.
 */
public abstract class Job<T extends Job.JobInfo> {
    @NonNull protected final T jobInfo;

    Job(@NonNull T jobInfo) {
        this.jobInfo = jobInfo;
    }

    abstract void execute(OkHttpClient okHttpClient, Callback callback);

    interface Callback {
        void onResult(Result result);
    }

    interface JobInfo extends Parcelable {
    }
}

