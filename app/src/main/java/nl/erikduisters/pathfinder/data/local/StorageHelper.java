package nl.erikduisters.pathfinder.data.local;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.VisibleForTesting;
import android.support.v4.content.ContextCompat;
import android.support.v4.os.EnvironmentCompat;

import java.io.File;

import javax.inject.Inject;
import javax.inject.Singleton;

import nl.erikduisters.pathfinder.di.ApplicationContext;
import timber.log.Timber;

/**
 * Created by Erik Duisters on 04-06-2018.
 */
@Singleton
public class StorageHelper {
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    public static final String ADOPTED_STORAGE_PREFIX = "/mnt/expand/";
    private static final String EMULATED_STORAGE_PREFIX = "/storage/emulated/";

    private Context ctx;

    @Inject
    public StorageHelper(@ApplicationContext Context ctx) {
        Timber.e("new StorageHelper created");
        this.ctx = ctx;
    }

    public File getFilesDir() {
        return ctx.getFilesDir();
    }

    public File getCacheDir() {
        return ctx.getCacheDir();
    }

    public File getExternalFilesDir() { return ctx.getExternalFilesDir(null); }
    public File[] getExternalFilesDirs() {
        return ContextCompat.getExternalFilesDirs(ctx, null);
    }

    public File getExternalCacheDir() { return ctx.getExternalCacheDir(); }
    public File[] getExternalCacheDirs() {
        return ContextCompat.getExternalCacheDirs(ctx);
    }

    public boolean isInternal(File storage) {
        return storage.equals(getFilesDir());
    }

    public boolean isStorageMounted(File storage) {
        return EnvironmentCompat.getStorageState(storage).equals(Environment.MEDIA_MOUNTED);
    }

    public boolean isStorageAdopted(File storage) {
        return storage.getAbsolutePath().startsWith(ADOPTED_STORAGE_PREFIX);
    }

    public boolean isStorageEmulated(File storage) {
        return storage.getAbsolutePath().startsWith(EMULATED_STORAGE_PREFIX);
    }
}
