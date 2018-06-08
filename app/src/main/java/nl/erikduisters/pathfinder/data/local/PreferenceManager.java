package nl.erikduisters.pathfinder.data.local;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.File;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import nl.erikduisters.pathfinder.R;
import nl.erikduisters.pathfinder.di.ApplicationContext;
import timber.log.Timber;

/**
 * Created by Erik Duisters on 04-06-2018.
 */

@Singleton
public class PreferenceManager {
    private final String KEY_STORAGE_DIRECTORY;
    private final String KEY_CACHE_DIRECTORY;
    private final String KEY_STORAGE_IMPORT_DIRECTORY;
    private final String KEY_STORAGE_UUID;
    private final String KEY_STORAGE_CACHE_DIRECTORY;
    private final String KEY_STORAGE_MAP_DIRECTORY;
    private final String KEY_STORAGE_USER_DIRECTORY;
    private final String KEY_STORAGE_RENDERTHEME_DIRECTORY;

    private final SharedPreferences preferences;
    private String storageDir;
    private String cacheDir;

    @Inject
    public PreferenceManager(@ApplicationContext Context context) {
        Timber.e("new PreferenceManager created");
        android.preference.PreferenceManager.setDefaultValues(context, R.xml.preferences, true);

        KEY_STORAGE_DIRECTORY = context.getString(R.string.key_storage_directory);
        KEY_CACHE_DIRECTORY = context.getString(R.string.key_cache_directory);
        KEY_STORAGE_IMPORT_DIRECTORY = context.getString(R.string.key_storage_import_directory);
        KEY_STORAGE_UUID = context.getString(R.string.key_storage_uuid);
        KEY_STORAGE_CACHE_DIRECTORY = context.getString(R.string.key_storage_cache_directory);
        KEY_STORAGE_MAP_DIRECTORY = context.getString(R.string.key_storage_map_directory);
        KEY_STORAGE_USER_DIRECTORY = context.getString(R.string.key_storage_user_directory);
        KEY_STORAGE_RENDERTHEME_DIRECTORY = context.getString(R.string.key_storage_rendertheme_directory);

        preferences = android.preference.PreferenceManager.getDefaultSharedPreferences(context);
        storageDir = getStorageDir();
        cacheDir = getCacheDir();
    }

    public synchronized String getStorageDir() {
        if (storageDir == null) {
            storageDir = preferences.getString(KEY_STORAGE_DIRECTORY, "");
        }

        return storageDir;
    }

    public synchronized void setStorageDir(String dir) {
        storageDir = dir.endsWith(File.separator) ? dir : dir + File.separator;

        SharedPreferences.Editor e = preferences.edit();
        e.putString(KEY_STORAGE_DIRECTORY, storageDir);
        e.apply();
    }

    public synchronized String getCacheDir() {
        if (cacheDir == null) {
            cacheDir = preferences.getString(KEY_CACHE_DIRECTORY, "");
        }

        return cacheDir;
    }

    public synchronized  void setCacheDir(String dir) {
        cacheDir = dir.endsWith(File.separator) ? dir : dir + File.separator;

        SharedPreferences.Editor e = preferences.edit();
        e.putString(KEY_CACHE_DIRECTORY, cacheDir);
        e.apply();
    }

    public synchronized String getStorageImportSubDir() {
        return preferences.getString(KEY_STORAGE_IMPORT_DIRECTORY, "Import");
    }

    public synchronized String getStorageCacheSubDir() {
        return preferences.getString(KEY_STORAGE_CACHE_DIRECTORY, "Cache");
    }

    public synchronized String getStorageMapSubDir() {
        return preferences.getString(KEY_STORAGE_MAP_DIRECTORY, "Maps");
    }

    public synchronized String getStorageUserSubDir() {
        return preferences.getString(KEY_STORAGE_USER_DIRECTORY, "User");
    }

    public synchronized String getStorageRenderThemeSubDir() {
        return preferences.getString(KEY_STORAGE_RENDERTHEME_DIRECTORY, "RenderThemes");
    }

    public synchronized UUID getStorageUUID() {
        String uuid = preferences.getString(KEY_STORAGE_UUID, "");

        return uuid.isEmpty() ? null : UUID.fromString(uuid);
    }

    public synchronized void setStorageUUID(UUID uuid) {
        SharedPreferences.Editor e = preferences.edit();
        e.putString(KEY_STORAGE_UUID, uuid == null ? "" : uuid.toString());
        e.apply();
    }
}