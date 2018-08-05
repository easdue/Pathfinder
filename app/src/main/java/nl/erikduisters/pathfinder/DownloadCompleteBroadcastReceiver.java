package nl.erikduisters.pathfinder;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

import nl.erikduisters.pathfinder.service.MapDownloadService;
import timber.log.Timber;

import static android.app.DownloadManager.EXTRA_DOWNLOAD_ID;

/**
 * Created by Erik Duisters on 02-08-2018.
 */
public class DownloadCompleteBroadcastReceiver extends android.content.BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Timber.e("Broadcast Received: %s", intent.toString());

        if (intent.hasExtra(EXTRA_DOWNLOAD_ID)) {
            long downloadId = intent.getLongExtra(EXTRA_DOWNLOAD_ID, 0);

            Intent startServiceIntent = new Intent(context.getApplicationContext(), MapDownloadService.class);
            startServiceIntent.putExtra(EXTRA_DOWNLOAD_ID, downloadId);

            if (Build.VERSION.SDK_INT >= 26) {
                context.startForegroundService(startServiceIntent);
            } else {
                context.startService(startServiceIntent);
            }
        }
    }
}
