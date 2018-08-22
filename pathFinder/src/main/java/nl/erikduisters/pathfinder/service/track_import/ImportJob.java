package nl.erikduisters.pathfinder.service.track_import;

import android.content.Context;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Created by Erik Duisters on 22-08-2018.
 */
public abstract class ImportJob<T extends ImportJob.JobInfo> {
    protected @NonNull T jobInfo;
    protected volatile boolean isCanceled;

    public ImportJob(@NonNull T jobInfo) {
        this.jobInfo = jobInfo;
    }

    public abstract int numTracksToImport();
    public abstract void removeTrack(String trackIdentifier);
    public abstract @NonNull String getTrackIdentifier(int track);
    public abstract @NonNull List<String> getTrackIdentifiers();
    public void cancel() { isCanceled = true; }

    /**
     * Returns an InputStream to the GPX data
     *
     * @param track The track to import
     * @param context Context to use
     * @param callback The Callback to use
     * @return The InputStream to the GPX data or null when Callback.isCanceled returns true
     * @throws RuntimeException
     * @throws IOException
     */
    public @Nullable
    abstract InputStream getInputStream(int track, Context context, Callback callback) throws RuntimeException, IOException;

    abstract void cleanupResource(int track);

    public interface Callback {
        void showNotification(String title, String text);
    }

    public interface JobInfo extends Parcelable {}
}
