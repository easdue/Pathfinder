package nl.erikduisters.pathfinder.service.track_import;

import android.annotation.TargetApi;
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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.oscim.utils.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import nl.erikduisters.pathfinder.R;
import nl.erikduisters.pathfinder.service.gpsies_service.GPSiesService;
import nl.erikduisters.pathfinder.util.NetworkUtil;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import timber.log.Timber;

/**
 * Created by Erik Duisters on 22-08-2018.
 */
public class GPSiesImportJob extends ImportJob<GPSiesImportJob.JobInfo> {
    private final Context context;
    private final OkHttpClient okHttpClient;
    private ConnectivityManager connectivityManager;
    private final ConnectivityBroadCastReceiver connectivityBroadCastReceiver;
    private final NetworkCallback networkCallback;
    private final Object waitLock;

    public GPSiesImportJob(@NonNull JobInfo jobInfo, Context context, OkHttpClient okHttpClient) {
        super(jobInfo);

        this.context = context;
        this.okHttpClient = okHttpClient;

        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        connectivityBroadCastReceiver = new ConnectivityBroadCastReceiver();

        if (Build.VERSION.SDK_INT >= 21) {
            networkCallback = new NetworkCallback();
        } else {
            networkCallback = null;
        }

        waitLock = new Object();
    }

    @Override
    public int numTracksToImport() {
        return jobInfo.trackFileIds.size();
    }

    @Override
    public void removeTrack(String trackIdentifier) {
        jobInfo.trackFileIds.remove(trackIdentifier);
    }

    @Override
    public @NonNull String getTrackIdentifier(int track) {
        return jobInfo.trackFileIds.get(track);
    }

    @Override
    public @NonNull List<String> getTrackIdentifiers() {
        return jobInfo.trackFileIds;

    }

    @Override
    public void cancel() {
        super.cancel();

        if (Build.VERSION.SDK_INT < 21) {
            context.unregisterReceiver(connectivityBroadCastReceiver);
        } else {
            connectivityManager.unregisterNetworkCallback(networkCallback);
        }

        synchronized (waitLock) {
            waitLock.notify();
        }
    }

    @Override
    public @Nullable InputStream getInputStream(int track, Context context, Callback callback) throws RuntimeException, IOException {
        if (!NetworkUtil.isNetworkConnected(context)) {
            callback.showNotification(context.getString(R.string.notification_title_downloading_tracks), context.getString(R.string.notification_text_waiting_for_network));
            waitForNetwork(callback);
        }

        if (isCanceled) {
            return null;
        }

        Request request = GPSiesService.getDownloadTrackRequest(jobInfo.trackFileIds.get(track));

        try {
            Response response = okHttpClient.newCall(request).execute();

            if (response.isSuccessful()) {
                Timber.d("Successful response received");

                ResponseBody responseBody = response.body();
                //noinspection ConstantConditions
                MediaType mediaType = responseBody.contentType();

                if (mediaType == null || (mediaType.type().equals("application") && mediaType.subtype().equals("gpx+xml"))) {
                    return responseBody.byteStream();
                }
            } else {
                IOUtils.closeQuietly(response);
                throw new RuntimeException("GPSiesImportJob received code: " + response.code() + " with message: " + response.message());
            }
        } catch (IOException e) {
            Timber.d("Connecting to GPSies.com failed: %s", e.getMessage());

            if (!NetworkUtil.isNetworkConnected(context)) {
                callback.showNotification(context.getString(R.string.notification_title_downloading_tracks), context.getString(R.string.notification_text_waiting_for_network));
                waitForNetwork(callback);

                if (isCanceled) {
                    return null;
                }
            } else {
                throw e;
            }
        }

        return null;
    }

    @Override
    void cleanupResource(int track) {
        return;
    }

    private void waitForNetwork(Callback callback) {
        if (Build.VERSION.SDK_INT < 21) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

            context.registerReceiver(connectivityBroadCastReceiver, filter);
        } else {
            NetworkRequest.Builder builder = new NetworkRequest.Builder();

            builder.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);

            connectivityManager.registerNetworkCallback(builder.build(), networkCallback);
        }

        synchronized (waitLock) {
            while (!isCanceled && !NetworkUtil.isNetworkConnected(context)) {
                try {
                    waitLock.wait();
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }

    private class ConnectivityBroadCastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                if (NetworkUtil.isNetworkConnected(context)) {
                    context.unregisterReceiver(this);

                    synchronized (waitLock) {
                        waitLock.notify();
                    }
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

            synchronized (waitLock) {
                waitLock.notify();
            }
        }
    }

    public static class JobInfo implements ImportJob.JobInfo {
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
}
