package nl.erikduisters.pathfinder.service.gpsies_service;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.crashlytics.android.Crashlytics;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import nl.erikduisters.gpx.GPXReader;
import nl.erikduisters.gpx.GpsiesGPXReader;
import nl.erikduisters.gpx.model.Gpx;
import nl.erikduisters.pathfinder.R;
import nl.erikduisters.pathfinder.data.local.PreferenceManager;
import nl.erikduisters.pathfinder.data.local.TrackRepository;
import nl.erikduisters.pathfinder.data.model.FullTrack;
import nl.erikduisters.pathfinder.di.GPSiesOkHttpClient;
import nl.erikduisters.pathfinder.ui.activity.main_activity.MainActivity;
import nl.erikduisters.pathfinder.util.JobId;
import nl.erikduisters.pathfinder.util.NetworkUtil;
import nl.erikduisters.pathfinder.util.NotificationChannels;
import nl.erikduisters.pathfinder.util.NotificationId;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import timber.log.Timber;

/**
 * Created by Erik Duisters on 17-08-2018.
 */

//TODO: Who ever came up with the JobIntentService?
//          - There is no way to define job scheduling criteria.
//          - When running as a job there is no way to know if the job has been re-scheduled
//TODO: So, since the user is expecting these downloads to happen just use a service/intentService that does work in the foreground
public class GPSiesTrackImportService extends JobIntentService {
    public static final String ACTION_DOWNLOAD_TRACKS = "nl.erikduisters.pathfinder.DOWNLOAD_TRACKS";
    public static final String EXTRA_DOWNLOAD_TRACKS_JOB_INFO = "nl.erikduisters.pathfinder.DOWNLOAD_TRACKS_JOB_INFO";

    private static final String ACTION_CANCEL_DOWLOADS = "nl.erikduisters.pathfinder.CANCEL_DOWNLOADS";

    @Inject
    @GPSiesOkHttpClient
    OkHttpClient okHttpClient;

    @Inject
    PreferenceManager preferenceManager;
    @Inject
    TrackRepository trackRepository;

    private GpsiesGPXReader gpxReader;
    private NotificationCompat.Builder notificationBuilder;
    private NotificationManagerCompat notificationManager;
    private ConnectivityManager connectivityManager;

    private final Object lock;
    private volatile boolean canceled;
    private final CancelBroadCastReceiver cancelBroadCastReceiver;
    private final ConnectivityBroadCastReceiver connectivityBroadCastReceiver;
    private final NetworkCallback networkCallback;

    public GPSiesTrackImportService() {
        lock = new Object();
        cancelBroadCastReceiver = new CancelBroadCastReceiver();
        connectivityBroadCastReceiver = new ConnectivityBroadCastReceiver();

        if (Build.VERSION.SDK_INT >= 21) {
            networkCallback = new NetworkCallback();
        } else {
            networkCallback = null;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        AndroidInjection.inject(this);

        gpxReader = new GpsiesGPXReader();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_CANCEL_DOWLOADS);

        registerReceiver(cancelBroadCastReceiver, intentFilter);

        Intent cancelIntent = new Intent(ACTION_CANCEL_DOWLOADS);
        //Without requestCode broadcast is not send
        PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(this, NotificationId.DOWNLOADING_TRACKS, cancelIntent, 0);

        notificationBuilder =
                new NotificationCompat.Builder(this, NotificationChannels.DOWNLOADING_TRACKS.getChannelId())
                        .setContentTitle(getString(R.string.notification_title_downloading_tracks))
                        .setSmallIcon(R.drawable.ic_notification_pathfinder)
                        .setPriority(NotificationChannels.DOWNLOADING_TRACKS.getImportance())
                        .setOngoing(true)
                        .setOnlyAlertOnce(true)
                        .setAutoCancel(false)
                        .addAction(new NotificationCompat.Action(0, getString(R.string.cancel), cancelPendingIntent));

        notificationManager = NotificationManagerCompat.from(this);

        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    public static void enqueueWork(Context context, Intent work) {
        if (work.getAction() == null || !work.getAction().equals(ACTION_DOWNLOAD_TRACKS)) {
            throw new IllegalArgumentException("GPSiesDownloadTracksService can only handle intents with action ACTION_DOWNLOAD_TRACKS");
        }

        if (work.getExtras() == null || !work.getExtras().containsKey(EXTRA_DOWNLOAD_TRACKS_JOB_INFO)) {
            throw new IllegalArgumentException("Intent must include an EXTRA_DOWNLOAD_TRACKS_JOB_INFO extra");
        }

        enqueueWork(context, GPSiesTrackImportService.class, JobId.GPSIES_DOWNLOAD_TRACKS_SERVICE_JOB_ID, work);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        JobInfo jobInfo = intent.getParcelableExtra(EXTRA_DOWNLOAD_TRACKS_JOB_INFO);

        List<String> downloadedTrackFileIds = getAlreadyDownloadedTrackFileIds(jobInfo.trackFileIds);
        removeDownloadedTrackFileIds(jobInfo.trackFileIds, downloadedTrackFileIds);

        if (jobInfo.trackFileIds.size() == 0) {
            return;
        }

        if (!NetworkUtil.isNetworkConnected(this)) {
            showWaitingForNetworkNotification();
            waitForNetwork();
        }

        if (canceled) {
            resetDownloadPreferences();
            return;
        }

        int curTrackIdx = 0;
        int numTracks = jobInfo.trackFileIds.size();
        float progressIncrement = 100 / numTracks;
        int failedImports = 0;

        showProgressNotification(curTrackIdx + 1, numTracks, 0);

        while (!isStopped() && !canceled && curTrackIdx < numTracks) {
            String trackFileId = jobInfo.trackFileIds.get(curTrackIdx);

            Request request = new Request.Builder()
                    .url(GPSiesService.GPSIES_URL + "/download.do")
                    .post(createRequestBody(trackFileId))
                    .build();

            try (Response response = okHttpClient.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    Timber.d("Successful response received");

                    ResponseBody responseBody = response.body();
                    //noinspection ConstantConditions
                    MediaType mediaType = responseBody.contentType();

                    if (mediaType == null || (mediaType.type().equals("application") && mediaType.subtype().equals("gpx+xml"))) {
                        //System.setProperty("sjxp.debug", "true");
                        Gpx gpx = gpxReader.doImport(responseBody.byteStream());

                        FullTrack track = new FullTrack(gpx);

                        trackRepository.save(track);

                        downloadedTrackFileIds.add(trackFileId);
                        preferenceManager.setDownloadedTrackFileIds(downloadedTrackFileIds);

                        curTrackIdx++;
                        showProgressNotification(Math.min(curTrackIdx + 1, numTracks), numTracks, Math.round((curTrackIdx) * progressIncrement));
                    }
                } else {
                    Timber.d("Got an error response from GPSies.com, code: %d, message: %s", response.code(), response.message());

                    curTrackIdx++;
                    failedImports++;

                    Crashlytics.logException(new RuntimeException("GPSiesDownloadTracksService received code: " + response.code() + " with message: " + response.message()));
                }
            } catch (GPXReader.ImportException e) {
                Timber.d("GPXReader threw an ImportException: %s", e.getMessage());
                jobInfo.trackFileIds.remove(0);

                curTrackIdx++;
                failedImports++;

                Crashlytics.logException(e);                //TODO: Show a notification?
            } catch (IOException e) {
                Timber.d("Connecting to GPSies.com failed: %s", e.getMessage());

                if (!NetworkUtil.isNetworkConnected(this)) {
                    showWaitingForNetworkNotification();
                    waitForNetwork();

                    if (canceled) {
                        resetDownloadPreferences();
                        return;
                    }
                } else {
                    curTrackIdx++;

                    Crashlytics.logException(e);
                }
            }
        }

        if (curTrackIdx >= numTracks) {
            Intent contentIntent = new Intent(this, MainActivity.class);
            contentIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

            /* Extreme bullshit again. If requestCode == 0 then MainActivity will always be re-created */
            PendingIntent pendingIntent = PendingIntent.getActivity(this, NotificationId.DOWNLOADING_TRACKS, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            notificationBuilder
                    .setContentTitle(getString(R.string.notification_title_finished_downloading_tracks))
                    .setContentText(getString(R.string.notification_text_finished_downloading_tracks, numTracks - failedImports, failedImports))
                    .setProgress(0, 0, false)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setOngoing(false)
                    .mActions.clear();

            notificationManager.notify(NotificationId.DOWNLOADING_TRACKS, notificationBuilder.build());
        }

        resetDownloadPreferences();

        unregisterReceiver(cancelBroadCastReceiver);
    }

    private List<String> getAlreadyDownloadedTrackFileIds(List<String> trackFileIds) {
        List<String> downloadingTrackFileIds = preferenceManager.getDownloadingTrackFileIds();
        List<String> downloadedTrackFileIds;

        if (!downloadingTrackFileIds.equals(trackFileIds)) {
            preferenceManager.setDownloadingTrackFileIds(trackFileIds);
            preferenceManager.setDownloadedTrackFileIds(Collections.emptyList());
            downloadedTrackFileIds = new ArrayList<>();
        } else {
            downloadedTrackFileIds = preferenceManager.getDownloadedTrackFileIds();
        }

        return downloadedTrackFileIds;
    }

    private void removeDownloadedTrackFileIds(List<String> trackFileIdsToDownload, List<String> downloadedTrackFileIds) {
        for (String trackFileId : downloadedTrackFileIds) {
            trackFileIdsToDownload.remove(trackFileId);
        }
    }

    private void resetDownloadPreferences() {
        preferenceManager.setDownloadedTrackFileIds(Collections.emptyList());
        preferenceManager.setDownloadingTrackFileIds(Collections.emptyList());
    }

    private void waitForNetwork() {
        if (Build.VERSION.SDK_INT < 21) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

            registerReceiver(connectivityBroadCastReceiver, filter);
        } else {
            NetworkRequest.Builder builder = new NetworkRequest.Builder();

            builder.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);

            connectivityManager.registerNetworkCallback(builder.build(), networkCallback);
        }

        synchronized (lock) {
            while (!canceled && !NetworkUtil.isNetworkConnected(this)) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }

    private void showProgressNotification(int curTrack, int numTracks, int progress) {
        notificationBuilder
                .setContentTitle(getString(R.string.notification_title_downloading_tracks))
                .setContentText(getString(R.string.notification_text_downloading_track, curTrack, numTracks))
                .setProgress(100, Math.round(progress), false);
        notificationManager.notify(NotificationId.DOWNLOADING_TRACKS, notificationBuilder.build());
    }

    private void showWaitingForNetworkNotification() {
        notificationBuilder
                .setContentTitle(getString(R.string.notification_title_downloading_tracks))
                .setContentText(getString(R.string.notification_text_waiting_for_network))
                .setProgress(0, 0, false);
        notificationManager.notify(NotificationId.DOWNLOADING_TRACKS, notificationBuilder.build());
    }

    private RequestBody createRequestBody(String trackFileId) {
        return new FormBody.Builder()
                .add("trackSimplification", "0")    //TODO: Douglas-Peucker: none=0 low=0.00001 middle=0.00005 high=0.0001
                .add("fileId", trackFileId)
                .add("dataType", "3")
                .add("filetype", "gpxTrk")
                .build();
    }

    public static class JobInfo implements Parcelable {
        @NonNull final List<String> trackFileIds;

        public JobInfo(@NonNull List<String> trackFileIds) {
            this.trackFileIds = trackFileIds;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeStringList(this.trackFileIds);
        }

        protected JobInfo(Parcel in) {
            this.trackFileIds = in.createStringArrayList();
        }

        public static final Creator<JobInfo> CREATOR = new Creator<JobInfo>() {
            @Override
            public JobInfo createFromParcel(Parcel source) {
                return new JobInfo(source);
            }

            @Override
            public JobInfo[] newArray(int size) {
                return new JobInfo[size];
            }
        };
    }

    private class ConnectivityBroadCastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                if (NetworkUtil.isNetworkConnected(context)) {
                    unregisterReceiver(this);

                    synchronized (lock) {
                        lock.notify();
                    }
                }
            }
        }
    }

    private class CancelBroadCastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_CANCEL_DOWLOADS)) {
                unregisterReceiver(this);

                notificationManager.cancel(NotificationId.DOWNLOADING_TRACKS);

                canceled = true;

                if (Build.VERSION.SDK_INT < 21) {
                    unregisterReceiver(connectivityBroadCastReceiver);
                } else {
                    connectivityManager.unregisterNetworkCallback(networkCallback);
                }

                synchronized (lock) {
                    lock.notify();
                }
            }
        }
    }

    @TargetApi(21)
    private class NetworkCallback extends ConnectivityManager.NetworkCallback {
        @Override
        public void onAvailable(Network network) {
            super.onAvailable(network);

            connectivityManager.unregisterNetworkCallback(this);

            synchronized (lock) {
                lock.notify();
            }
        }
    }
}
