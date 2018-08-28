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

package nl.erikduisters.pathfinder.data.usecase;

import android.app.DownloadManager;
import android.database.Cursor;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import nl.erikduisters.pathfinder.async.Cancellable;

/**
 * Created by Erik Duisters on 28-07-2018.
 */

public class DownloadStatusChecker extends UseCase<DownloadStatusChecker.RequestInfo, DownloadStatusChecker.DownloadStatus> {
    private int numAvailableDownloads;
    private int numFailedDownloads;
    private int numPendingDownloads;

    public DownloadStatusChecker(@NonNull RequestInfo requestInfo, @NonNull Callback<DownloadStatus> callback) {
        super(requestInfo, callback);
    }

    @Override
    public void execute(Cancellable cancellable) {
        DownloadManager.Query query = new DownloadManager.Query();

        try {
            Cursor cursor = requestInfo.downloadManager.query(query);

            DownloadStatus downloadStatus = getDownloadStatus(cursor);

            cursor.close();

            if (!cancellable.isCancelled()) {
                getCallback().onResult(downloadStatus);
            } else {
            }
        } catch (Exception e) {
            callback.onError(e);
        }
    }

    private DownloadStatus getDownloadStatus(Cursor cursor) {
        if (cursor.getCount() == 0) {
            return new DownloadStatus();
        }

        getCounts(cursor);

        List<MapInfo> availableMapDownloads = getMapInfoForDownloadsWithStatus(DownloadManager.STATUS_SUCCESSFUL, cursor);
        List<MapInfo> failedMapDownloads = getMapInfoForDownloadsWithStatus(DownloadManager.STATUS_FAILED, cursor);

        return new DownloadStatus(failedMapDownloads, numPendingDownloads, availableMapDownloads);
    }

    private void getCounts(Cursor cursor) {
        numAvailableDownloads = numFailedDownloads = numPendingDownloads = 0;

        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));

            switch(status) {
                case DownloadManager.STATUS_SUCCESSFUL:
                    numAvailableDownloads++;
                    break;
                case DownloadManager.STATUS_FAILED:
                    numFailedDownloads++;
                    break;
                case DownloadManager.STATUS_PAUSED:
                case DownloadManager.STATUS_PENDING:
                case DownloadManager.STATUS_RUNNING:
                    numPendingDownloads++;
                    break;
            }

            cursor.moveToNext();
        }
    }

    private @NonNull List<MapInfo> getMapInfoForDownloadsWithStatus(int downloadManagerStatus, Cursor cursor) {
        List<MapInfo> availableMapDownloads = new ArrayList<>(numAvailableDownloads);

        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));

            if (status == downloadManagerStatus) {
                long id = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_ID));
                String uri = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                String title = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_TITLE));

                MapInfo availableMapDownload = new MapInfo(id, uri, title);

                availableMapDownloads.add(availableMapDownload);
            }

            cursor.moveToNext();
        }

        return availableMapDownloads;
    }

    public static class RequestInfo {
        final DownloadManager downloadManager;

        public RequestInfo(DownloadManager downloadManager) {
            this.downloadManager = downloadManager;
        }
    }

    public static class DownloadStatus {
        @NonNull public final List<MapInfo> availableMapDownloadsInfoList;
        public final List<MapInfo> failedMapDownloadsInfoList;
        public final int numPendingDownloads;

        public DownloadStatus() {
            this(new ArrayList<>(0), 0, new ArrayList<>(0));
        }

        public DownloadStatus(@NonNull List<MapInfo> failedMapDownloadsInfoList,
                              int numPendingDownloads,
                              @NonNull List<MapInfo> availableMapDownloadsInfoList) {
            this.failedMapDownloadsInfoList = failedMapDownloadsInfoList;
            this.numPendingDownloads = numPendingDownloads;
            this.availableMapDownloadsInfoList = availableMapDownloadsInfoList;
        }
    }

    public static class MapInfo {
        public final long mapDownloadId;
        public final String mapUri;
        public final String title;

        public MapInfo(long mapDownloadId, String uri, String title) {
            this.mapDownloadId = mapDownloadId;
            this.mapUri = uri;
            this.title = title;
        }
    }
}
