/*
 * Copyright (c) 2018 Erik Duisters
 *
 * This file is part of Pathfinder
 *
 * Pathfinder is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Foobar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Pathfinder. If not, see <https://www.gnu.org/licenses/>.
 */

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
