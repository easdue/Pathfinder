package nl.erikduisters.pathfinder.service.track_import;

import android.content.Context;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Erik Duisters on 22-08-2018.
 */
public class LocalImportJob extends ImportJob<LocalImportJob.JobInfo> {
    public LocalImportJob(@NonNull JobInfo jobInfo) {
        super(jobInfo);
    }

    @Override
    public int numTracksToImport() {
        return jobInfo.filesToImport.size();
    }

    @Override
    public void removeTrack(String trackIdentifier) {
        Iterator<File> it = jobInfo.filesToImport.iterator();

        while (it.hasNext()) {
            File file = it.next();

            if (file.getAbsolutePath().equals(trackIdentifier)) {
                it.remove();
                return;
            }
        }
    }

    @NonNull
    @Override
    public String getTrackIdentifier(int track) {
        return jobInfo.filesToImport.get(track).getAbsolutePath();
    }

    @NonNull
    @Override
    public List<String> getTrackIdentifiers() {
        List<String> trackIdentifiers = new ArrayList<>(jobInfo.filesToImport.size());

        for (File file : jobInfo.filesToImport) {
            trackIdentifiers.add(file.getAbsolutePath());
        }

        return trackIdentifiers;
    }

    @Nullable
    @Override
    public InputStream getInputStream(int track, Context context, Callback callback) throws RuntimeException, IOException {
        File file = jobInfo.filesToImport.get(track);

        return new FileInputStream(file);
    }

    @Override
    void cleanupResource(int track) {
        jobInfo.filesToImport.get(track).delete();
    }

    public static class JobInfo implements ImportJob.JobInfo {
        private final List<File> filesToImport;

        public JobInfo(List<File> filesToImport) {
            this.filesToImport = filesToImport;
        }


        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeList(this.filesToImport);
        }

        protected JobInfo(Parcel in) {
            this.filesToImport = new ArrayList<>();
            in.readList(this.filesToImport, File.class.getClassLoader());
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
