package nl.erikduisters.pathfinder.service.gpsies_service;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import nl.erikduisters.pathfinder.di.GPSiesOkHttpClient;
import nl.erikduisters.pathfinder.util.NetworkUtil;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import timber.log.Timber;

/**
 * Created by Erik Duisters on 11-08-2018.
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
    @GPSiesOkHttpClient
    OkHttpClient okHttpClient;

    private LocalBroadcastManager localBroadcastManager;

    public GPSiesService() {
        super(GPSiesService.class.getSimpleName());
        setIntentRedelivery(false);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Timber.d("onCreate()");

        AndroidInjection.inject(this);

        localBroadcastManager = LocalBroadcastManager.getInstance(this);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent == null) {
            return;
        }

        if (!NetworkUtil.isNetworkConnected(this)) {
            onResult(new Result.NoNetworAvailableError());
            return;
        }

        if (intent.getAction() == null) {
            return;
        }

        if (intent.getAction().equals(ACTION_SEARCH_TRACKS)) {
            handleSearchTrackIntent(intent);
        }
    }

    private void handleSearchTrackIntent(@NonNull Intent intent) {
        if (intent.getExtras() == null || !intent.getExtras().containsKey(EXTRA_SEARCH_JOB_INFO)) {
            throw new IllegalStateException("An intent with action: ACTION_SEARCH_TRACKS must contain an EXTRA_SEARCH_JOB_INFO extra");
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

    public static Request getDownloadTrackRequest(String trackFileId) {
        return new Request.Builder()
                .url(GPSIES_URL + "/download.do")
                .post(createTrackDownloadRequestBody(trackFileId))
                .build();
    }

    private static RequestBody createTrackDownloadRequestBody(String trackFileId) {
        return new FormBody.Builder()
                .add("trackSimplification", "0")    //TODO: Douglas-Peucker: none=0 low=0.00001 middle=0.00005 high=0.0001
                .add("fileId", trackFileId)
                .add("dataType", "3")
                .add("filetype", "gpxTrk")
                .build();
    }
}
