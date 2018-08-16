package nl.erikduisters.pathfinder.service.gpsies_service;

import okhttp3.OkHttpClient;

/**
 * Created by Erik Duisters on 12-08-2018.
 */
public abstract class Job {
    interface Callback {
        void onResult(Result result);
    }
    /*
        SearchTracks
        LoadTrackGeoJsonData
     */
    abstract void execute(OkHttpClient okHttpClient, Callback callback);

}

