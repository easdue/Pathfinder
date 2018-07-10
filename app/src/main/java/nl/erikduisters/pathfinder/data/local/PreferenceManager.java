package nl.erikduisters.pathfinder.data.local;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;

import org.oscim.core.MapPosition;
import org.oscim.theme.ExternalRenderTheme;
import org.oscim.theme.IRenderTheme;
import org.oscim.theme.ThemeFile;
import org.oscim.theme.VtmThemes;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import nl.erikduisters.pathfinder.R;
import nl.erikduisters.pathfinder.data.model.map.OnlineMap;
import nl.erikduisters.pathfinder.data.model.map.ScaleBarType;
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
    private static final int DEFAULT_ZOOM_LEVEL = 17;

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
    private final String KEY_USE_OFFLINE_MAP;
    private final String KEY_OFFLINE_MAP;
    private final String KEY_ONLINE_MAP;
    private final String KEY_USE_EXTERNAL_RENDER_THEME;
    private final String KEY_INTERNAL_RENDER_THEME;
    private final String KEY_EXTERNAL_RENDER_THEME;
    private final String KEY_RENDER_THEME_STYLE;
    private final String KEY_MAP_FOLLOWS_GPS;
    private final String KEY_MAP_LATITUDE;
    private final String KEY_MAP_LONGITUDE;
    private final String KEY_MAP_ZOOM_LEVEL;
    private final String KEY_MAP_SCALE_BAR_TYPE;

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
        KEY_STORAGE_RENDERTHEME_DIRECTORY = context.getString(R.string.key_storage_render_theme_directory);
        KEY_LAST_KNOWN_LOCATION_LATITUDE = context.getString(R.string.key_last_known_location_latitude);
        KEY_LAST_KNOWN_LOCATION_LONGITUDE = context.getString(R.string.key_last_known_location_longitude);
        KEY_LAST_KNOWN_LOCATION_TIME = context.getString(R.string.key_last_known_location_time);
        KEY_ASK_TO_ENABLE_GPS = context.getString(R.string.key_ask_to_enable_gps);
        KEY_ASK_TO_RESOLVE_PLAY_SERVICES_UNAVAILABILITY = context.getString(R.string.key_ask_to_resolve_play_services_unavailability);
        KEY_USE_OFFLINE_MAP = context.getString(R.string.key_map_use_offline_map);
        KEY_OFFLINE_MAP = context.getString(R.string.key_map_offline_map);
        KEY_ONLINE_MAP = context.getString(R.string.key_map_online_map);
        KEY_USE_EXTERNAL_RENDER_THEME = context.getString(R.string.key_map_use_external_render_theme);
        KEY_INTERNAL_RENDER_THEME = context.getString(R.string.key_map_internal_render_theme);
        KEY_EXTERNAL_RENDER_THEME = context.getString(R.string.key_map_external_render_theme);
        KEY_RENDER_THEME_STYLE = context.getString(R.string.key_map_render_theme_style);
        KEY_MAP_FOLLOWS_GPS = context.getString(R.string.key_map_follows_gps);
        KEY_MAP_LATITUDE = context.getString(R.string.key_map_latitude);
        KEY_MAP_LONGITUDE = context.getString(R.string.key_map_longitude);
        KEY_MAP_ZOOM_LEVEL = context.getString(R.string.key_map_zoom_level);
        KEY_MAP_SCALE_BAR_TYPE = context.getString(R.string.key_map_scale_bar_type);

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

        preferences.edit()
                .putString(KEY_STORAGE_DIRECTORY, storageDir)
                .apply();
    }

    public synchronized String getCacheDir() {
        if (cacheDir == null) {
            cacheDir = preferences.getString(KEY_CACHE_DIRECTORY, "");
        }

        return cacheDir;
    }

    public synchronized  void setCacheDir(String dir) {
        cacheDir = FileUtil.ensureEndsWithSeparator(dir);

        preferences.edit()
                .putString(KEY_CACHE_DIRECTORY, cacheDir)
                .apply();
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
        preferences.edit()
                .putString(KEY_STORAGE_UUID, uuid == null ? "" : uuid.toString())
                .apply();
    }

    public synchronized Location getLastKnownLocation() {
        Location loc = new Location(LocationManager.GPS_PROVIDER);
        loc.setLatitude(Double.parseDouble(preferences.getString(KEY_LAST_KNOWN_LOCATION_LATITUDE, DEFAULT_LATITUDE)));
        loc.setLongitude(Double.parseDouble(preferences.getString(KEY_LAST_KNOWN_LOCATION_LONGITUDE, DEFAULT_LONGITUDE)));
        loc.setTime(preferences.getLong(KEY_LAST_KNOWN_LOCATION_TIME, 0));

        return loc;
    }

    public synchronized void setLastKnownLocation(Location loc) {
        preferences.edit()
                .putString(KEY_LAST_KNOWN_LOCATION_LATITUDE, String.valueOf(loc.getLatitude()))
                .putString(KEY_LAST_KNOWN_LOCATION_LONGITUDE, String.valueOf(loc.getLongitude()))
                .putLong(KEY_LAST_KNOWN_LOCATION_TIME, loc.getTime())
                .apply();
    }

    public synchronized boolean askToEnableGps() {
        return preferences.getBoolean(KEY_ASK_TO_ENABLE_GPS, true);
    }

    public synchronized void setAskToEnableGps(boolean ask) {
        preferences.edit()
                .putBoolean(KEY_ASK_TO_ENABLE_GPS, ask)
                .apply();
    }

    public synchronized boolean askToResolvePlayServicesUnavailability() {
        return preferences.getBoolean(KEY_ASK_TO_RESOLVE_PLAY_SERVICES_UNAVAILABILITY, true);
    }

    public synchronized void setAskToResolvePlayServicesUnavailability(boolean ask) {
        preferences.edit()
                .putBoolean(KEY_ASK_TO_RESOLVE_PLAY_SERVICES_UNAVAILABILITY, ask)
                .apply();
    }

    public synchronized boolean useOfflineMap() {
        return preferences.getBoolean(KEY_USE_OFFLINE_MAP, false);
    }

    public synchronized void setUseOfflineMap(boolean use) {
        preferences.edit()
                .putBoolean(KEY_USE_OFFLINE_MAP, use)
                .apply();
    }

    @NonNull
    public synchronized String getOfflineMap() {
        return preferences.getString(KEY_OFFLINE_MAP, "");
    }

    public synchronized OnlineMap getOnlineMap() throws IllegalArgumentException {
        return OnlineMap.valueOf(preferences.getString(KEY_ONLINE_MAP, OnlineMap.OSCIMAP4.name()));
    }

    public synchronized boolean useExternalRenderTheme() {
        return preferences.getBoolean(KEY_USE_EXTERNAL_RENDER_THEME, false);
    }

    public synchronized VtmThemes getInternalRenderTheme() {
        String theme = preferences.getString(KEY_INTERNAL_RENDER_THEME, "DEFAULT").toUpperCase();
        return VtmThemes.valueOf(theme);
    }

    @NonNull
    public synchronized ThemeFile getExternalRenderTheme() throws IRenderTheme.ThemeException {
        StringBuilder builder = new StringBuilder();
        builder.append(getStorageDir());
        builder.append(getStorageRenderThemeSubDir());
        builder.append(preferences.getString(KEY_EXTERNAL_RENDER_THEME, "NoSuchTheme.xml"));

        return new ExternalRenderTheme(builder.toString());
    }

    public synchronized void setExternalRenderTheme(String path) {
        preferences.edit()
                .putString(KEY_EXTERNAL_RENDER_THEME, path)
                .apply();
    }

    @NonNull
    public synchronized String getRenderThemeStyle() {
        return preferences.getString(KEY_RENDER_THEME_STYLE, "");
    }

    public synchronized void setRenderThemeStyle(String style) {
        preferences.edit()
                .putString(KEY_RENDER_THEME_STYLE, style)
                .apply();
    }

    public synchronized boolean mapFollowsGps() {
        return preferences.getBoolean(KEY_MAP_FOLLOWS_GPS, true);
    }

    public synchronized void setMapFollowsGps(boolean mapFollowsGps) {
        preferences.edit()
                .putBoolean(KEY_MAP_FOLLOWS_GPS, mapFollowsGps)
                .apply();
    }

    public synchronized MapPosition getMapPosition() {
        MapPosition mapPosition = new MapPosition();
        double latitude = Double.parseDouble(preferences.getString(KEY_MAP_LATITUDE, DEFAULT_LATITUDE));
        double longitude = Double.parseDouble(preferences.getString(KEY_MAP_LONGITUDE, DEFAULT_LONGITUDE));

        mapPosition.setPosition(latitude, longitude);
        mapPosition.setZoomLevel(preferences.getInt(KEY_MAP_ZOOM_LEVEL, DEFAULT_ZOOM_LEVEL));

        return mapPosition;
    }

    public synchronized void setMapPosition(MapPosition mapPosition) {
        preferences.edit()
                .putString(KEY_MAP_LATITUDE, String.valueOf(mapPosition.getLatitude()))
                .putString(KEY_MAP_LONGITUDE, String.valueOf(mapPosition.getLongitude()))
                .putInt(KEY_MAP_ZOOM_LEVEL, mapPosition.getZoomLevel())
                .apply();
    }

    public synchronized @ScaleBarType int getScaleBarType() {
        return preferences.getInt(KEY_MAP_SCALE_BAR_TYPE, ScaleBarType.METRIC_AND_IMPERIAL);
    }
}
