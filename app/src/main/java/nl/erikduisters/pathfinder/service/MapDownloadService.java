package nl.erikduisters.pathfinder.service;

import android.app.DownloadManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import dagger.android.AndroidInjection;
import nl.erikduisters.pathfinder.R;
import nl.erikduisters.pathfinder.async.BackgroundJobHandler;
import nl.erikduisters.pathfinder.async.ProgressUseCaseJob;
import nl.erikduisters.pathfinder.async.UseCaseJob;
import nl.erikduisters.pathfinder.data.local.PreferenceManager;
import nl.erikduisters.pathfinder.data.usecase.DownloadStatusChecker;
import nl.erikduisters.pathfinder.data.usecase.DownloadStatusChecker.DownloadStatus;
import nl.erikduisters.pathfinder.data.usecase.UnzipMap;
import nl.erikduisters.pathfinder.data.usecase.UnzipMap.Result.UnzipFailed.Reason;
import nl.erikduisters.pathfinder.ui.activity.main_activity.MainActivity;
import nl.erikduisters.pathfinder.util.NotificationChannels;
import nl.erikduisters.pathfinder.util.NotificationId;
import timber.log.Timber;

/**
 * Created by Erik Duisters on 28-07-2018.
 */

/*
 * DownloadManager renames the file and alters COLUMN_LOCAL_URI when a download with the same name already exists
 * and the old file is deleted (why?) but its record is unmodified
 */

//TODO: Create animating notification icon like android.R.drawable.stat_sys_download
//TODO: Take user to map settings fragment when user clicks on available maps notification (eg. start SettingsActivity, add main SettingsFragment, add mapSettingsFragment to create proper backstack (MainActivity->SettingsActivity->SettingsFragment->SettingsFragment) and don't forget to call MapDownloadService.cleanupMapAvailableNotification)
public class MapDownloadService extends Service {
    @Inject PreferenceManager preferenceManager;
    @Inject BackgroundJobHandler backgroundJobHandler;
    @Inject @Named("MainLooper") Handler handler;

    private final IBinder binder;
    private DownloadManager downloadManager;
    private final List<Listener> listeners;
    private final DownloadStatusCheckerCallback downloadCheckerCallback;
    private final UnzipMapCallback unzipMapCallback;
    private final StartMapDownloadCheckerRunnable startMapDownloadCheckerRunnable;
    private UseCaseJob currentDownloadStatusCheckerJob;
    private ProgressUseCaseJob currentUnzipMapJob;
    private boolean inForeground;
    private DownloadStatus lastDownloadStatus;
    private NotificationCompat.Builder notificationBuilder;
    private static NotificationCompat.InboxStyle inboxStyle;

    public MapDownloadService() {
        Timber.e("new MapDownloadService created");

        binder = new MapDownloadServiceBinder();
        listeners = new ArrayList<>();

        downloadCheckerCallback = new DownloadStatusCheckerCallback();
        unzipMapCallback = new UnzipMapCallback();
        startMapDownloadCheckerRunnable = new StartMapDownloadCheckerRunnable();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Timber.e("onCreate()");

        AndroidInjection.inject(this);

        downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Timber.e("onDestroy()");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Timber.e("onStartCommand");

        if (!inForeground) {
            startForeground();
        }

        if (currentUnzipMapJob == null || !backgroundJobHandler.isRunning(currentUnzipMapJob)) {
            Timber.d("onStartCommand: calling startMapDownloadChecker()");
            startMapDownloadChecker();
        } else {
            Timber.d("onStartCommand: UnzipMapJob@%s already running", currentUnzipMapJob);
        }

        return START_STICKY;
    }

    private void startForeground() {
        notificationBuilder =
                new NotificationCompat.Builder(this, NotificationChannels.UNZIPPING_MAPS.getChannelId())
                        .setContentTitle(getString(R.string.extracting_maps))
                        .setSmallIcon(R.drawable.ic_notification_unarchive)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setOngoing(true)
                        .setOnlyAlertOnce(true)
                        .setAutoCancel(false);

        Notification notification = notificationBuilder.build();

        startForeground(NotificationId.MAP_DOWNLOAD_SERVICE_RUNNING_IN_FOREGROUND, notification);

        inForeground = true;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Timber.e("onBind()");
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Timber.e("onUnbind()");
        return false;
    }

    public static void cleanupMapAvailableNotification() {
        MapDownloadService.inboxStyle = null;
    }

    public void retryRetryableMapDownloads() {
        List<Long> retryableMapDownloadIds = preferenceManager.getRetryableMapDownloadIds();

        if (retryableMapDownloadIds.size() > 0) {
            retryableMapDownloadIds.clear();
            preferenceManager.setRetryableMapDownloadIds(retryableMapDownloadIds);
            preferenceManager.setMapsAreDownloading(true);

            if (listeners.size() > 0) {
                startMapDownloadChecker();
            }
        }
    }

    public void addListener(@NonNull Listener listener) {
        if (listeners.contains(listener)) {
            return;
        }

        listeners.add(listener);

        if (listeners.size() == 1 && !inForeground) {
            startMapDownloadChecker();
        }
    }

    public void removeListener(@NonNull Listener listener) {
        if (listeners.contains(listener)) {
            listeners.remove(listener);
        }

        if (listeners.size() == 0) {
            if (currentDownloadStatusCheckerJob != null && backgroundJobHandler.isRunning(currentDownloadStatusCheckerJob)) {
                Timber.d("removeListener: Cancelling StatusCheckerJob@%s", currentDownloadStatusCheckerJob);
                currentDownloadStatusCheckerJob.cancel();
                currentDownloadStatusCheckerJob = null;
            }
        }
    }

    public void enqueue(DownloadManager.Request request) {
        downloadManager.enqueue(request);
        preferenceManager.setMapsAreDownloading(true);

        if (listeners.size() > 0) {
            Timber.d("enqueue: calling startMapDownloadChecker()");
            startMapDownloadChecker();
        }
    }

    private void startMapDownloadChecker() {
        Timber.d("startMapDownloadChecker: Cancelling StartMapDownloadCheckerRunnable");
        handler.removeCallbacks(startMapDownloadCheckerRunnable);

        if (preferenceManager.areMapsDownloading()) {
            if (currentUnzipMapJob != null && backgroundJobHandler.isRunning(currentUnzipMapJob)) {
                Timber.d("startMapDownloadChecker: Already running UnzipMabJob@%s", currentDownloadStatusCheckerJob);
                return;
            }

            if (currentDownloadStatusCheckerJob != null && backgroundJobHandler.isRunning(currentDownloadStatusCheckerJob)) {
                Timber.d("startMapDownloadChecker: Canceling DownloadStatusCheckerJob@%s", currentDownloadStatusCheckerJob);
                currentDownloadStatusCheckerJob.cancel();
                currentDownloadStatusCheckerJob = null;
            }

            DownloadStatusChecker.RequestInfo requestInfo = new DownloadStatusChecker.RequestInfo(downloadManager);
            DownloadStatusChecker useCase = new DownloadStatusChecker(requestInfo, downloadCheckerCallback);

            currentDownloadStatusCheckerJob = useCase.getUseCaseJob();
            backgroundJobHandler.runJob(currentDownloadStatusCheckerJob);
        } else {
            Timber.d("startMapDownloadChecker: preferenceManager.areMapsDownloading() returned false, calling listeners.onDownloadFinished()");

            for (Listener listener : listeners) {
                listener.onMapDownloadComplete();
            }
        }
    }

    public interface Listener {
        void onMapAvailable(String mapName);
        void onMapDownloadFailed(String zipFileName);
        void onFailedMapDownloadsAvailable(int numFailedDownloads);
        void onRetryableMapDownloadsAvailable(int numRetryableDownloads);
        void onMapUnzipFailed(String zipFileName);
        void onMapDownloadComplete();
    }

    public class MapDownloadServiceBinder extends Binder {
        public MapDownloadService getService() {
            return MapDownloadService.this;
        }
    }

    private class DownloadStatusCheckerCallback implements DownloadStatusChecker.Callback<DownloadStatus> {
        @Override
        public void onResult(@NonNull DownloadStatus downloadStatus) {
            Timber.e("MapDownloadCheckerCallback.onResult()");

            handleDownloadStatus(downloadStatus);
        }

        @Override
        public void onError(@NonNull Throwable error) {
            Timber.e("MapDownloadCheckerCallback.onError()");

            currentDownloadStatusCheckerJob = null;

            //A job for Crashlytics
            throw new RuntimeException(error);
        }
    }

    private void handleDownloadStatus(DownloadStatus downloadStatus) {
        if (currentUnzipMapJob != null && backgroundJobHandler.isRunning(currentUnzipMapJob)) {
            Timber.d("handleDownloadStatus: Ignoring because UnzipMapJob@%s is still running", currentUnzipMapJob);
            return;
        }

        Timber.d("handleDownloadStatus: numPending = %d, numFailed= %d, numAvailable = %d", downloadStatus.numPendingDownloads, downloadStatus.failedMapDownloadsInfoList.size(), downloadStatus.availableMapDownloadsInfoList.size());

        currentDownloadStatusCheckerJob = null;
        lastDownloadStatus = downloadStatus;

        updateRetryableMapDownloadIdsFromDownloadStatus(downloadStatus);
        removeRetryableMapDownloadIdsFromDownloadStatus(downloadStatus);
        reportRetryableDownloads();

        updateReportedFailedMapDownloadIdsFromDownloadStatus(downloadStatus);
        reportFailedDownloads(downloadStatus);
        reportFailedDownloadsAvailable();
        removeFailedDownloadFiles(downloadStatus);

        if (downloadStatus.availableMapDownloadsInfoList.size() > 0) {
            handleAvailableMapDownloads(downloadStatus);
        } else {
            if (inForeground) {
                stopForeground(true);
                inForeground = false;
                stopSelf();
            }

            if (downloadStatus.numPendingDownloads == 0) {
                if (downloadStatus.failedMapDownloadsInfoList.size() == 0) {
                    Timber.d("handleDownloadStatus: calling preferenceManager.setMapsAreDownloading(false)");
                    preferenceManager.setMapsAreDownloading(false);
                }

                Timber.d("Calling listeners.onMapDownloadComplete()");
                for (Listener listener : listeners) {
                    listener.onMapDownloadComplete();
                }
            } else {
                if (listeners.size() > 0) {
                    Timber.d("handleDownloadStatus: scheduling StartMapDownloadCheckerRunnable");
                    handler.postDelayed(startMapDownloadCheckerRunnable, 5000);
                }
            }
        }
    }

    private void updateRetryableMapDownloadIdsFromDownloadStatus(DownloadStatus downloadStatus) {
        List<Long> retryableMapDownloadIds = preferenceManager.getRetryableMapDownloadIds();

        List<Long> availableMapIds = new ArrayList<>(downloadStatus.availableMapDownloadsInfoList.size());

        for (DownloadStatusChecker.MapInfo mapInfo : downloadStatus.availableMapDownloadsInfoList) {
            availableMapIds.add(mapInfo.mapDownloadId);
        }

        removeFromListIfNoLongerAvailable(retryableMapDownloadIds, availableMapIds);

        preferenceManager.setRetryableMapDownloadIds(retryableMapDownloadIds);
    }

    private void removeFromListIfNoLongerAvailable(List<Long> removeFrom, List<Long> available) {
        Iterator<Long> it = removeFrom.iterator();

        while (it.hasNext()) {
            Long lookingFor = it.next();

            if (!available.contains(lookingFor)) {
                it.remove();
            }
        }
    }

    private void removeRetryableMapDownloadIdsFromDownloadStatus(DownloadStatus downloadStatus) {
        List<Long> retryableMapDownloadIds = preferenceManager.getRetryableMapDownloadIds();

        Iterator<DownloadStatusChecker.MapInfo> it = downloadStatus.availableMapDownloadsInfoList.iterator();

        while (it.hasNext()) {
            DownloadStatusChecker.MapInfo mapInfo = it.next();

            if (retryableMapDownloadIds.contains(mapInfo.mapDownloadId)) {
                it.remove();
            }
        }
    }

    private void updateReportedFailedMapDownloadIdsFromDownloadStatus(DownloadStatus downloadStatus) {
        List<Long> reportedFailedMapDownloadIds = preferenceManager.getReportedFailedMapDownloadIds();

        List<Long> failedMapIds = new ArrayList<>(downloadStatus.failedMapDownloadsInfoList.size());

        for (DownloadStatusChecker.MapInfo mapInfo : downloadStatus.failedMapDownloadsInfoList) {
            failedMapIds.add(mapInfo.mapDownloadId);
        }

        removeFromListIfNoLongerAvailable(reportedFailedMapDownloadIds, failedMapIds);

        preferenceManager.setReportedFailedMapDownloadIds(failedMapIds);
    }

    private void reportRetryableDownloads() {
        List<Long> retryableDownloadIds = preferenceManager.getRetryableMapDownloadIds();

        for (Listener listener : listeners) {
            listener.onRetryableMapDownloadsAvailable(retryableDownloadIds.size());
        }
    }

    private void reportFailedDownloads(DownloadStatus downloadStatus) {
        List<Long> reportedFailedMapDownloadIds = preferenceManager.getReportedFailedMapDownloadIds();

        for (DownloadStatusChecker.MapInfo mapInfo : downloadStatus.failedMapDownloadsInfoList) {
            if (!reportedFailedMapDownloadIds.contains(mapInfo.mapDownloadId)) {
                for (Listener listener : listeners) {
                    listener.onMapDownloadFailed(mapInfo.title);
                }

                reportedFailedMapDownloadIds.add(mapInfo.mapDownloadId);
            }
        }

        preferenceManager.setReportedFailedMapDownloadIds(reportedFailedMapDownloadIds);
    }

    private void reportFailedDownloadsAvailable() {
        List<Long> reportedFaileMapDownloadIds = preferenceManager.getReportedFailedMapDownloadIds();

        for (Listener listener : listeners) {
            listener.onFailedMapDownloadsAvailable(reportedFaileMapDownloadIds.size());
        }
    }

    /* DownloadManager creates an empty file for each failed download and if the download is restarted a new file is created with a -<number> suffix */
    private void removeFailedDownloadFiles(DownloadStatus downloadStatus) {
        for (DownloadStatusChecker.MapInfo mapInfo : downloadStatus.failedMapDownloadsInfoList) {
            if (mapInfo.mapUri == null) {
                continue;
            }

            Uri mapUri = Uri.parse(mapInfo.mapUri);

            if (mapUri.getScheme().equals("file")) {
                File mapFile = new File(mapUri.getPath());

                if (mapFile.isFile()) {
                    mapFile.delete();
                }
            }
        }
    }

    private void handleAvailableMapDownloads(DownloadStatus downloadStatus) {
        if (inForeground) {
            handleAvailableMapDownload(downloadStatus.availableMapDownloadsInfoList.get(0));
        } else {
            Intent intent = new Intent(this, MapDownloadService.class);
            startService(intent);
        }
    }

    private void handleAvailableMapDownload(DownloadStatusChecker.MapInfo availableMapInfo) {
        File mapDir = preferenceManager.getStorageMapDir();
        Uri zippedMapFileUri = Uri.parse(availableMapInfo.mapUri);

        UnzipMap.RequestInfo requestInfo =
                new UnzipMap.RequestInfo(availableMapInfo.mapDownloadId, availableMapInfo.title, mapDir, zippedMapFileUri, getContentResolver());
        UnzipMap useCase = new UnzipMap(requestInfo, unzipMapCallback);

        currentUnzipMapJob = useCase.getUseCaseJob();
        backgroundJobHandler.runJob(currentUnzipMapJob);
    }

    private class UnzipMapCallback implements UnzipMap.Callback<UnzipMap.Progress, UnzipMap.Result> {
        @Override
        public void onResult(@NonNull UnzipMap.Result result) {
            currentUnzipMapJob = null;

            if (result instanceof UnzipMap.Result.UnzipFinished) {
                handleFinishedUnzipResult((UnzipMap.Result.UnzipFinished) result);
            }

            if (result instanceof UnzipMap.Result.UnzipFailed) {
                handleFailedUnzipResult((UnzipMap.Result.UnzipFailed) result);
            }

            startMapDownloadChecker();
        }

        @Override
        public void onProgress(@NonNull UnzipMap.Progress progress) {
            Timber.d("onProgress: map: %s, progress: %d", progress.mapName, progress.progress);
            handleUnzipProgress(progress);
        }

        @Override
        public void onError(@NonNull Throwable error) {
            handleUnzipError(error);
        }
    }

    private void handleFinishedUnzipResult(UnzipMap.Result.UnzipFinished result) {
        downloadManager.remove(result.mapDownloadId);

        showMapAvailableNotification(result.mapName);

        for (Listener listener : listeners) {
            listener.onMapAvailable(result.mapName);
        }
    }

    private void showMapAvailableNotification(String mapName) {
        if (inboxStyle == null) {
            inboxStyle = new NotificationCompat.InboxStyle();
        }

        inboxStyle.addLine("\t• " + mapName);

        //TODO: Only show if there are no listener? (eg app is not visible)
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(MainActivity.INTENT_EXTRA_STARTED_FROM_MAP_AVAILABLE_NOTIFICATION, true);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(intent);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, NotificationChannels.MAPS_AVAILABLE.getChannelId())
                        .setContentTitle(getString(R.string.available_maps_title))
                        .setContentText(getString(R.string.available_maps_description))
                        .setSmallIcon(R.drawable.ic_notification_pathfinder)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setOngoing(false)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent)
                        .setDeleteIntent(pendingIntent)
                        .setOnlyAlertOnce(true)
                        .setStyle(inboxStyle);

        Notification notification = builder.build();

        NotificationManagerCompat.from(this)
                .notify(NotificationId.MAP_AVAILABLE, notification);
    }

    private void handleFailedUnzipResult(UnzipMap.Result.UnzipFailed result) {
        switch(result.reason) {
            case Reason.FILE_NOT_FOUND:
                downloadManager.remove(result.mapDownloadId);
                break;
            case Reason.NOT_ENOUGH_SPACE_AVAILABLE:
                List<Long> retryableMapDownloadIds = preferenceManager.getRetryableMapDownloadIds();

                if (!retryableMapDownloadIds.contains(result.mapDownloadId)) {
                    retryableMapDownloadIds.add(result.mapDownloadId);
                    preferenceManager.setRetryableMapDownloadIds(retryableMapDownloadIds);
                }

                for (Listener listener : listeners) {
                    listener.onMapUnzipFailed(result.zipFileName);
                }
                break;
            case Reason.ZIPFILE_CORRUPTED:
                downloadManager.remove(result.mapDownloadId);

                for (Listener listener : listeners) {
                    listener.onMapDownloadFailed(result.zipFileName);
                }
                break;
        }
    }

    private void handleUnzipProgress(UnzipMap.Progress progress) {
        notificationBuilder.setContentText(getString(R.string.extracting_map, progress.mapName));
        if (Float.isNaN(progress.progress)) {
            notificationBuilder.setProgress(0, 0, true);
        } else {
            notificationBuilder.setProgress(100, Math.round(progress.progress), false);
        }

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);

        notificationManagerCompat.notify(NotificationId.MAP_DOWNLOAD_SERVICE_RUNNING_IN_FOREGROUND, notificationBuilder.build());
    }

    private void handleUnzipError(Throwable error) {
        //TODO: When does this happen and is the error recoverable or not
        currentUnzipMapJob = null;

        UnzipMap.RequestInfo requestInfo = (UnzipMap.RequestInfo)currentUnzipMapJob.getUseCase().getRequestInfo();

        downloadManager.remove(requestInfo.mapDownloadId);

        for (Listener listener : listeners) {
            listener.onMapUnzipFailed(requestInfo.zipFileName);
        }

        startMapDownloadChecker();
    }

    private class StartMapDownloadCheckerRunnable implements Runnable {
        @Override
        public void run() {
            Timber.d("postDelayed(): Calling startMapDownloadChecker()");
            startMapDownloadChecker();
        }
    }
}
