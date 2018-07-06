package nl.erikduisters.pathfinder.data.local;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import nl.erikduisters.pathfinder.R;
import nl.erikduisters.pathfinder.di.ApplicationContext;
import nl.erikduisters.pathfinder.util.FileUtil;
import timber.log.Timber;

/**
 * Created by Erik Duisters on 04-06-2018.
 */

@Singleton
public class PreferenceManager {
    private static final String DEFAULT_LATITUDE = "52.3700";
    private static final String DEFAULT_LONGITUDE = "4.8900";

    private final String KEY_STORAGE_DIRECTORY;
    private final String KEY_CACHE_DIRECTORY;
    private final String KEY_STORAGE_IMPORT_DIRECTORY;
    private final String KEY_STORAGE_UUID;
    private final String KEY_STORAGE_CACHE_DIRECTORY;
    private final String KEY_STORAGE_MAP_DIRECTORY;
    private final String KEY_STORAGE_USER_DIRECTORY;
    private final String KEY_STORAGE_RENDERTHEME_DIRECTORY;
    private final String KEY_LAST_KNOWN_LOCATION_LATITUDE;
    private final String KEY_LAST_KNOWN_LOCATION_LONGITUDE;
    private final String KEY_LAST_KNOWN_LOCATION_TIME;
    private final String KEY_ASK_TO_ENABLE_GPS;
    private final String KEY_ASK_TO_RESOLVE_PLAY_SERVICES_UNAVAILABILITY;

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
        KEY_LAST_KNOWN_LOCATION_LATITUDE = context.getString(R.string.key_last_known_location_latitude);
        KEY_LAST_KNOWN_LOCATION_LONGITUDE = context.getString(R.string.key_last_known_location_longitude);
        KEY_LAST_KNOWN_LOCATION_TIME = context.getString(R.string.key_last_known_location_time);
        KEY_ASK_TO_ENABLE_GPS = context.getString(R.string.key_ask_to_enable_gps);
        KEY_ASK_TO_RESOLVE_PLAY_SERVICES_UNAVAILABILITY = context.getString(R.string.key_ask_to_resolve_play_services_unavailability);

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
        storageDir = FileUtil.ensureEndsWithSeparator(dir);

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
        cacheDir = FileUtil.ensureEndsWithSeparator(dir);

        SharedPreferences.Editor e = preferences.edit();
        e.putString(KEY_CACHE_DIRECTORY, cacheDir);
        e.apply();
    }

    public synchronized String getStorageImportSubDir() {
        return FileUtil.ensureEndsWithSeparator(preferences.getString(KEY_STORAGE_IMPORT_DIRECTORY, "Import"));

    }

    public synchronized String getStorageCacheSubDir() {
        return FileUtil.ensureEndsWithSeparator(preferences.getString(KEY_STORAGE_CACHE_DIRECTORY, "Cache"));
    }

    public synchronized String getStorageMapSubDir() {
        return FileUtil.ensureEndsWithSeparator(preferences.getString(KEY_STORAGE_MAP_DIRECTORY, "Maps"));
    }

    public synchronized String getStorageUserSubDir() {
        return FileUtil.ensureEndsWithSeparator(preferences.getString(KEY_STORAGE_USER_DIRECTORY, "User"));
    }

    public synchronized String getStorageRenderThemeSubDir() {
        return FileUtil.ensureEndsWithSeparator(preferences.getString(KEY_STORAGE_RENDERTHEME_DIRECTORY, "RenderThemes"));
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

    public synchronized Location getLastKnownLocation() {
        Location loc = new Location(LocationManager.GPS_PROVIDER);
        loc.setLatitude(Double.parseDouble(preferences.getString(KEY_LAST_KNOWN_LOCATION_LATITUDE, DEFAULT_LATITUDE)));
        loc.setLongitude(Double.parseDouble(preferences.getString(KEY_LAST_KNOWN_LOCATION_LONGITUDE, DEFAULT_LONGITUDE)));
        loc.setTime(preferences.getLong(KEY_LAST_KNOWN_LOCATION_TIME, 0));

        return loc;
    }

    public synchronized void setLastKnownLocation(Location loc) {
        SharedPreferences.Editor e = preferences.edit();

        e.putString(KEY_LAST_KNOWN_LOCATION_LATITUDE, String.valueOf(loc.getLatitude()));
        e.putString(KEY_LAST_KNOWN_LOCATION_LONGITUDE, String.valueOf(loc.getLongitude()));
        e.putLong(KEY_LAST_KNOWN_LOCATION_TIME, loc.getTime());

        e.apply();
    }

    public synchronized boolean askToEnableGps() {
        return preferences.getBoolean(KEY_ASK_TO_ENABLE_GPS, true);
    }

    public synchronized void setAskToEnableGps(boolean ask) {
        SharedPreferences.Editor e = preferences.edit();

        e.putBoolean(KEY_ASK_TO_ENABLE_GPS, ask);
        e.apply();
    }

    public synchronized boolean askToResolvePlayServicesUnavailability() {
        return preferences.getBoolean(KEY_ASK_TO_RESOLVE_PLAY_SERVICES_UNAVAILABILITY, true);
    }

    public synchronized void setAskToResolvePlayServicesUnavailability(boolean ask) {
        SharedPreferences.Editor e = preferences.edit();

        e.putBoolean(KEY_ASK_TO_RESOLVE_PLAY_SERVICES_UNAVAILABILITY, ask);
        e.apply();
    }
}
