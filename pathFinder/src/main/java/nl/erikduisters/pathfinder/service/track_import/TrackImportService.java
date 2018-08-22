package nl.erikduisters.pathfinder.service.track_import;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.crashlytics.android.Crashlytics;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import nl.erikduisters.gpx.GpsiesGPXReader;
import nl.erikduisters.gpx.model.Gpx;
import nl.erikduisters.pathfinder.R;
import nl.erikduisters.pathfinder.data.local.PreferenceManager;
import nl.erikduisters.pathfinder.data.local.TrackRepository;
import nl.erikduisters.pathfinder.data.model.FullTrack;
import nl.erikduisters.pathfinder.di.GPSiesOkHttpClient;
import nl.erikduisters.pathfinder.ui.activity.main_activity.MainActivity;
import nl.erikduisters.pathfinder.util.JobId;
import nl.erikduisters.pathfinder.util.NotificationChannels;
import nl.erikduisters.pathfinder.util.NotificationId;
import okhttp3.OkHttpClient;
import timber.log.Timber;

/**
 * Created by Erik Duisters on 17-08-2018.
 */

//TODO: Who ever came up with the JobIntentService?
//          - There is no way to define job scheduling criteria.
//          - When running as a job there is no way to know if the job has been re-scheduled
//TODO: So, since the user is expecting these downloads to happen just use a service/intentService that does work in the foreground
public class TrackImportService extends JobIntentService implements ImportJob.Callback {
    public static final String ACTION_IMPORT_TRACKS = "nl.erikduisters.pathfinder.IMPORT_TRACKS";
    public static final String EXTRA_IMPORT_TRACKS_JOB_INFO = "nl.erikduisters.pathfinder.IMPORT_TRACKS_JOB_INFO";

    private static final String ACTION_CANCEL_IMPORT = "nl.erikduisters.pathfinder.CANCEL_IMPORT";

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

    private volatile boolean canceled;
    private final CancelBroadCastReceiver cancelBroadCastReceiver;
    private ImportJob currentImportJob;

    public TrackImportService() {
        cancelBroadCastReceiver = new CancelBroadCastReceiver();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        AndroidInjection.inject(this);

        gpxReader = new GpsiesGPXReader();

        Intent cancelIntent = new Intent(ACTION_CANCEL_IMPORT);
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
    }

    public static void enqueueWork(Context context, Intent work) {
        if (work.getAction() == null || !work.getAction().equals(ACTION_IMPORT_TRACKS)) {
            throw new IllegalArgumentException("GPSiesDownloadTracksService can only handle intents with action ACTION_IMPORT_TRACKS");
        }

        if (work.getExtras() == null || !work.getExtras().containsKey(EXTRA_IMPORT_TRACKS_JOB_INFO)) {
            throw new IllegalArgumentException("Intent must include an EXTRA_IMPORT_TRACKS_JOB_INFO extra");
        }

        enqueueWork(context, TrackImportService.class, JobId.IMPORT_TRACK_SERVICE_JOB_ID, work);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_CANCEL_IMPORT);

        registerReceiver(cancelBroadCastReceiver, intentFilter);

        ImportJob.JobInfo jobInfo = intent.getParcelableExtra(EXTRA_IMPORT_TRACKS_JOB_INFO);

        if (jobInfo instanceof GPSiesImportJob.JobInfo) {
            currentImportJob = new GPSiesImportJob((GPSiesImportJob.JobInfo) jobInfo, this, okHttpClient);
        } else if (jobInfo instanceof LocalImportJob.JobInfo) {
            currentImportJob = new LocalImportJob((LocalImportJob.JobInfo) jobInfo);
        } else {
            throw new IllegalStateException("I do not know how to handle: " + jobInfo.getClass().getName());
        }

        List<String> downloadedTrackIdentifiers = getAlreadyDownloadedTrackIdentifiers(currentImportJob.getTrackIdentifiers());
        removeDownloadedTracks(currentImportJob, downloadedTrackIdentifiers);

        if (currentImportJob.numTracksToImport() == 0) {
            return;
        }

        int curTrackIdx = 0;
        int numTracks = currentImportJob.numTracksToImport();
        float progressIncrement = 100 / numTracks;
        int failedImports = 0;

        showProgressNotification(curTrackIdx + 1, numTracks, 1);

        while (!isStopped() && !canceled && curTrackIdx < numTracks) {
            try (InputStream inputStream = currentImportJob.getInputStream(curTrackIdx, this, this)) {
                if (!canceled) {
                    //System.setProperty("sjxp.debug", "true");
                    Gpx gpx = gpxReader.doImport(inputStream);

                    FullTrack track = new FullTrack(gpx);

                    trackRepository.save(track);

                    currentImportJob.cleanupResource(curTrackIdx);

                    downloadedTrackIdentifiers.add(currentImportJob.getTrackIdentifier(curTrackIdx));
                    preferenceManager.setDownloadedTrackIdentifiers(downloadedTrackIdentifiers);

                    curTrackIdx++;
                    showProgressNotification(Math.min(curTrackIdx + 1, numTracks), numTracks, Math.round((curTrackIdx) * progressIncrement));
                }
            } catch (RuntimeException e) {
                Timber.d(e.getMessage());

                curTrackIdx++;
                showProgressNotification(Math.min(curTrackIdx + 1, numTracks), numTracks, Math.round((curTrackIdx) * progressIncrement));
                failedImports++;
                Crashlytics.logException(e);
            }  catch (IOException e) {
                curTrackIdx++;
                showProgressNotification(Math.min(curTrackIdx + 1, numTracks), numTracks, Math.round((curTrackIdx) * progressIncrement));
                failedImports++;
                Crashlytics.logException(e);
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

    private List<String> getAlreadyDownloadedTrackIdentifiers(@NonNull List<String> jobTrackIdentifiers) {
        List<String> downloadingTrackIdentifiers = preferenceManager.getDownloadingTrackIdentifiers();
        List<String> downloadedTrackFileIds;


        if (!downloadingTrackIdentifiers.equals(jobTrackIdentifiers)) {
            preferenceManager.setDownloadingTrackIdentifiers(jobTrackIdentifiers);
            preferenceManager.setDownloadedTrackIdentifiers(Collections.emptyList());
            downloadedTrackFileIds = new ArrayList<>();
        } else {
            downloadedTrackFileIds = preferenceManager.getDownloadedTrackIdentifiers();
        }

        return downloadedTrackFileIds;
    }

    private boolean jobMatches(@NonNull ImportJob importJob, @NonNull List<String> downloadingTrackIdentifiers) {
        if (importJob.numTracksToImport() != downloadingTrackIdentifiers.size()) {
            return false;
        }

        for (int i = 0, length = importJob.numTracksToImport(); i < length; i++) {
            if (!importJob.getTrackIdentifier(i).equals(downloadingTrackIdentifiers.get(i))) {
                return false;
            }
        }

        return true;
    }

    private void removeDownloadedTracks(ImportJob importJob, List<String> downloadedIdentifiers) {
        for (String trackIdentifier : downloadedIdentifiers) {
            importJob.removeTrack(trackIdentifier);
        }
    }

    private void resetDownloadPreferences() {
        preferenceManager.setDownloadedTrackIdentifiers(Collections.emptyList());
        preferenceManager.setDownloadingTrackIdentifiers(Collections.emptyList());
    }

    private void showProgressNotification(int curTrack, int numTracks, int progress) {
        notificationBuilder
                .setContentTitle(getString(R.string.notification_title_downloading_tracks))
                .setContentText(getString(R.string.notification_text_downloading_track, curTrack, numTracks))
                .setProgress(100, Math.round(progress), false);
        notificationManager.notify(NotificationId.DOWNLOADING_TRACKS, notificationBuilder.build());
    }

    @Override
    public void showNotification(String title, String text) {
        notificationBuilder
                .setContentTitle(getString(R.string.notification_title_downloading_tracks))
                .setContentText(getString(R.string.notification_text_waiting_for_network))
                .setProgress(0, 0, false);
        notificationManager.notify(NotificationId.DOWNLOADING_TRACKS, notificationBuilder.build());
    }

    private class CancelBroadCastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_CANCEL_IMPORT)) {
                currentImportJob.cancel();
                canceled = true;

                notificationManager.cancel(NotificationId.DOWNLOADING_TRACKS);
            }
        }
    }
}
