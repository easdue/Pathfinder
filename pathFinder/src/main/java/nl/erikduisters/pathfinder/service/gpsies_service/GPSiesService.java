package nl.erikduisters.pathfinder.service.gpsies_service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import okhttp3.OkHttpClient;
import timber.log.Timber;

/**
 * Created by Erik Duisters on 11-08-2018.
 */

/*
    TODO: Setup cookieJar: The height chart is generated with metric/imperial scale depending on cookies u=imperial|metric
                           As soon as I am able to login the cookie is also used to identify me

    CookieJar cookieJar = new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(context));

    OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .cookieJar(cookieJar)
                    .build();
 */
public class GPSiesService extends IntentService implements Job.Callback {
    public static final String BROADCAST_ACTION_RESULT = "nl.erikduisters.pathfinder.BROADCAST_RESULT";
    public static final String EXTRA_RESULT = "nl.erikduisters.pathfinder.RESULT";

    public static final String ACTION_SEARCH_TRACKS = "nl.erikduisters.pathfinder.SEARCH_TRACKS";
    public static final String EXTRA_SEARCH_JOB_INFO = "nl.erikduisters.pathfinder.EXTRA_SEARCH_JOB_INFO";

    static final String GPSIES_URL = "https://www.gpsies.com";

    public static Uri getHeightChartUri(String fileId, int width, int height) {
        Uri uri = Uri.parse(GPSIES_URL);

        return uri.buildUpon()
                .appendPath("media.do")
                .appendQueryParameter("fileId", fileId)
                .appendQueryParameter("width", String.valueOf(width))
                .appendQueryParameter("height", String.valueOf(height))
                .build();
    }

    @Inject
    OkHttpClient.Builder okHttpClientBuilder;
    OkHttpClient okHttpClient;

    private LocalBroadcastManager localBroadcastManager;

    public GPSiesService() {
        super(GPSiesService.class.getSimpleName());
        this.setIntentRedelivery(false);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Timber.d("onCreate()");

        AndroidInjection.inject(this);

        okHttpClient = okHttpClientBuilder
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();

        localBroadcastManager = LocalBroadcastManager.getInstance(this);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent == null) {
            return;
        }

        if (!networkAvailable()) {
            onResult(new Result.NoNetworAvailableError());
            return;
        }

        if (intent.getAction() != null && intent.getAction().equals(ACTION_SEARCH_TRACKS)) {
            handleSearchTrackIntent(intent);
        }
    }

    private boolean networkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        return activeNetwork != null && activeNetwork.isConnected();
    }

    private void handleSearchTrackIntent(@NonNull Intent intent) {
        if (!intent.getExtras().containsKey(EXTRA_SEARCH_JOB_INFO)) {
            throw new IllegalStateException("A intent with action: ACTION_SEARCH_TRACKS must contain an EXTRA_SEARCH_JOB_INFO extra");
        }

        SearchTracks.JobInfo jobInfo = intent.getParcelableExtra(EXTRA_SEARCH_JOB_INFO);
        new SearchTracks(jobInfo).execute(okHttpClient,this);
    }

    @Override
    public void onResult(Result result) {
        Intent intent = new Intent(BROADCAST_ACTION_RESULT);
        intent.putExtra(EXTRA_RESULT, result);

        localBroadcastManager.sendBroadcast(intent);
    }

}
