package nl.erikduisters.pathfinder.data.local;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.oscim.core.MapPosition;
import org.oscim.theme.IRenderTheme;
import org.oscim.theme.VtmThemes;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import nl.erikduisters.pathfinder.R;
import nl.erikduisters.pathfinder.di.ApplicationContext;
import nl.erikduisters.pathfinder.util.BoundingBox;
import nl.erikduisters.pathfinder.util.Coordinate;
import nl.erikduisters.pathfinder.util.Distance;
import nl.erikduisters.pathfinder.util.FileUtil;
import nl.erikduisters.pathfinder.util.Speed;
import nl.erikduisters.pathfinder.util.Units;
import nl.erikduisters.pathfinder.util.map.OnlineMap;
import nl.erikduisters.pathfinder.util.map.ScaleBarType;
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
    public  final String KEY_USE_ONLINE_MAP;
    public  final String KEY_OFFLINE_MAP;
    public  final String KEY_ONLINE_MAP;
    public  final String KEY_USE_INTERNAL_RENDER_THEME;
    public  final String KEY_INTERNAL_RENDER_THEME;
    public  final String KEY_EXTERNAL_RENDER_THEME;
    private final String KEY_RENDER_THEME_STYLE;
    private final String KEY_MAP_FOLLOWS_GPS;
    private final String KEY_MAP_LATITUDE;
    private final String KEY_MAP_LONGITUDE;
    private final String KEY_MAP_ZOOM_LEVEL;
    private final String KEY_MAP_TILT;
    private final String KEY_MAP_BEARING;
    public  final String KEY_MAP_SCALE_BAR_TYPE;
    public  final String KEY_MAP_DISPLAY_NORTH_UP;
    private final String KEY_MAP_BOUNDING_BOX_MIN_LATITUDE;
    private final String KEY_MAP_BOUNDING_BOX_MIN_LONGITUDE;
    private final String KEY_MAP_BOUNDING_BOX_MAX_LATITUDE;
    private final String KEY_MAP_BOUNDING_BOX_MAX_LONGITUDE;
    private final String KEY_USE_TRUE_NORTH;
    private final String KEY_USE_GPS_BEARING;
    private final String KEY_USE_GPS_BEARING_SPEED;
    private final String KEY_USE_GPS_BEARING_DURATION;
    private final String KEY_UNIT;
    private final String KEY_COORDINATE_DISPLAY_FORMAT;
    private final String KEY_DOWNLOADING_MAPS;
    private final String KEY_RETRYABLE_MAP_DOWNLOAD_IDS;
    private final String KEY_REPORTED_FAILED_MAP_DOWNLOAD_IDS;
    private final String KEY_DOWNLOADING_TRACK_IDENTIFIERS;
    private final String KEY_DOWNLOADED_TRACK_IDENTIFIERS;

    private final SharedPreferences preferences;
    private String storageDir;
    private String cacheDir;

    @Inject
    public PreferenceManager(@ApplicationContext Context context) {
        Timber.e("new PreferenceManager created");
        android.support.v7.preference.PreferenceManager.setDefaultValues(context, R.xml.preferences, true);
        //android.preference.PreferenceManager.setDefaultValues(context, R.xml.preferences, true);

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
        KEY_USE_ONLINE_MAP = context.getString(R.string.key_map_use_online_map);
        KEY_OFFLINE_MAP = context.getString(R.string.key_map_offline_map);
        KEY_ONLINE_MAP = context.getString(R.string.key_map_online_map);
        KEY_USE_INTERNAL_RENDER_THEME = context.getString(R.string.key_map_use_internal_render_theme);
        KEY_INTERNAL_RENDER_THEME = context.getString(R.string.key_map_internal_render_theme);
        KEY_EXTERNAL_RENDER_THEME = context.getString(R.string.key_map_external_render_theme);
        KEY_RENDER_THEME_STYLE = context.getString(R.string.key_map_render_theme_style);
        KEY_MAP_FOLLOWS_GPS = context.getString(R.string.key_map_follows_gps);
        KEY_MAP_LATITUDE = context.getString(R.string.key_map_latitude);
        KEY_MAP_LONGITUDE = context.getString(R.string.key_map_longitude);
        KEY_MAP_ZOOM_LEVEL = context.getString(R.string.key_map_zoom_level);
        KEY_MAP_TILT = context.getString(R.string.key_map_tilt);
        KEY_MAP_BEARING = context.getString(R.string.key_map_bearing);
        KEY_MAP_SCALE_BAR_TYPE = context.getString(R.string.key_map_scale_bar_type);
        KEY_MAP_DISPLAY_NORTH_UP = context.getString(R.string.key_map_display_north_up);
        KEY_MAP_BOUNDING_BOX_MIN_LATITUDE = context.getString(R.string.key_map_bounding_box_min_latitude);
        KEY_MAP_BOUNDING_BOX_MIN_LONGITUDE = context.getString(R.string.key_map_bounding_box_min_longitude);
        KEY_MAP_BOUNDING_BOX_MAX_LATITUDE = context.getString(R.string.key_map_bounding_box_max_latitude);
        KEY_MAP_BOUNDING_BOX_MAX_LONGITUDE = context.getString(R.string.key_map_bounding_box_max_longitude);
        KEY_USE_TRUE_NORTH = context.getString(R.string.key_use_true_north);
        KEY_USE_GPS_BEARING = context.getString(R.string.key_use_gps_bearing);
        KEY_USE_GPS_BEARING_SPEED = context.getString(R.string.key_gps_bearing_speed);
        KEY_USE_GPS_BEARING_DURATION = context.getString(R.string.key_gps_bearing_duration);
        KEY_UNIT = context.getString(R.string.key_unit);
        KEY_COORDINATE_DISPLAY_FORMAT = context.getString(R.string.key_coordinate_display_format);
        KEY_DOWNLOADING_MAPS = context.getString(R.string.key_downloading_maps);
        KEY_RETRYABLE_MAP_DOWNLOAD_IDS = context.getString(R.string.key_retryable_map_download_ids);
        KEY_REPORTED_FAILED_MAP_DOWNLOAD_IDS = context.getString(R.string.key_reported_failed_map_download_ids);
        KEY_DOWNLOADING_TRACK_IDENTIFIERS = context.getString(R.string.key_downloading_track_identifiers);
        KEY_DOWNLOADED_TRACK_IDENTIFIERS = context.getString(R.string.key_downloaded_track_identifiers);

        //preferences = android.preference.PreferenceManager.getDefaultSharedPreferences(context);
        preferences = android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences(context);
        storageDir = getStorageDir();
        cacheDir = getCacheDir();

        Coordinate.setDisplayFormat(getCoordinateDisplayFormat());
        Distance.setDisplayUnits(getUnits());
        Speed.setDisplayUnits(getUnits());
    }

    public void registerOnSharedPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        preferences.registerOnSharedPreferenceChangeListener(listener);
    }

    public void unregisterOnSharedPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        preferences.unregisterOnSharedPreferenceChangeListener(listener);
    }

    public synchronized String getStorageDir() {
        if (storageDir == null) {
            storageDir = preferences.getString(KEY_STORAGE_DIRECTORY, "");

            if (storageDir.endsWith(File.separator)) {
                setStorageDir(storageDir);
            }
        }

        return storageDir;
    }

    public synchronized void setStorageDir(String dir) {
        storageDir = FileUtil.removeEndSeparator(dir);

        preferences.edit()
                .putString(KEY_STORAGE_DIRECTORY, storageDir)
                .apply();
    }

    public synchronized String getCacheDir() {
        if (cacheDir == null) {
            cacheDir = preferences.getString(KEY_CACHE_DIRECTORY, "");

            if (cacheDir.endsWith(File.separator)) {
                setCacheDir(cacheDir);
            }
        }

        return cacheDir;
    }

    public synchronized  void setCacheDir(String dir) {
        cacheDir = FileUtil.removeEndSeparator(dir);

        preferences.edit()
                .putString(KEY_CACHE_DIRECTORY, cacheDir)
                .apply();
    }

    public synchronized String getStorageImportSubDir() {
        return FileUtil.removeEndSeparator(preferences.getString(KEY_STORAGE_IMPORT_DIRECTORY, "Import"));
    }

    public synchronized File getStorageImportDir() {
        return new File(storageDir, getStorageImportSubDir());
    }

    public synchronized String getStorageCacheSubDir() {
        return FileUtil.removeEndSeparator(preferences.getString(KEY_STORAGE_CACHE_DIRECTORY, "Cache"));
    }

    public synchronized File getStorageCacheDir() {
        return new File(storageDir, getStorageCacheSubDir());
    }

    public synchronized String getStorageMapSubDir() {
        return FileUtil.removeEndSeparator(preferences.getString(KEY_STORAGE_MAP_DIRECTORY, "Maps"));
    }

    public synchronized File getStorageMapDir() {
        return new File(storageDir, getStorageMapSubDir());
    }

    public synchronized String getStorageUserSubDir() {
        return FileUtil.removeEndSeparator(preferences.getString(KEY_STORAGE_USER_DIRECTORY, "User"));
    }

    public synchronized File getStorageUserDir() {
        return new File(storageDir, getStorageUserSubDir());
    }

    public synchronized String getStorageRenderThemeSubDir() {
        return FileUtil.removeEndSeparator(preferences.getString(KEY_STORAGE_RENDERTHEME_DIRECTORY, "RenderThemes"));
    }

    public synchronized File getStorageRenderThemeDir() {
        return new File(storageDir, getStorageRenderThemeSubDir());
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

    public synchronized boolean useOnlineMap() {
        return preferences.getBoolean(KEY_USE_ONLINE_MAP, true);
    }

    public synchronized void setUseOnlineMap(boolean use) {
        preferences.edit()
                .putBoolean(KEY_USE_ONLINE_MAP, use)
                .apply();
    }

    @NonNull
    public synchronized String getOfflineMap() {
        return preferences.getString(KEY_OFFLINE_MAP, "");
    }

    @NonNull
    public synchronized void setOfflineMap(String fileName) {
        preferences.edit()
                .putString(KEY_OFFLINE_MAP, fileName)
                .apply();
    }

    public synchronized OnlineMap getOnlineMap(){
        OnlineMap onlineMap;

        try {
            onlineMap = OnlineMap.valueOf(preferences.getString(KEY_ONLINE_MAP, OnlineMap.OSCIMAP4.name()));
        } catch (IllegalArgumentException e) {
            onlineMap = OnlineMap.OSCIMAP4;
        }

        return onlineMap;
    }

    public synchronized boolean useInternalRenderTheme() {
        return preferences.getBoolean(KEY_USE_INTERNAL_RENDER_THEME, true);
    }

    public synchronized void setUseInternalRenderTheme(boolean useExternalRenderTheme) {
        preferences.edit()
                .putBoolean(KEY_USE_INTERNAL_RENDER_THEME, useExternalRenderTheme)
                .apply();
    }

    public synchronized VtmThemes getInternalRenderTheme() {
        String theme = preferences.getString(KEY_INTERNAL_RENDER_THEME, "DEFAULT").toUpperCase();
        return VtmThemes.valueOf(theme);
    }

    @NonNull
    public synchronized String getExternalRenderThemeName() throws IRenderTheme.ThemeException {
        return preferences.getString(KEY_EXTERNAL_RENDER_THEME, "");
    }

    public synchronized void setExternalRenderThemeName(String renderThemeName) {
        preferences.edit()
                .putString(KEY_EXTERNAL_RENDER_THEME, renderThemeName)
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
        mapPosition.setTilt(preferences.getFloat(KEY_MAP_TILT, 0f));
        mapPosition.setBearing(preferences.getFloat(KEY_MAP_BEARING, 0f));

        return mapPosition;
    }

    public synchronized void setMapPosition(MapPosition mapPosition) {
        preferences.edit()
                .putString(KEY_MAP_LATITUDE, String.valueOf(mapPosition.getLatitude()))
                .putString(KEY_MAP_LONGITUDE, String.valueOf(mapPosition.getLongitude()))
                .putInt(KEY_MAP_ZOOM_LEVEL, mapPosition.getZoomLevel())
                .putFloat(KEY_MAP_TILT, mapPosition.getTilt())
                .putFloat(KEY_MAP_BEARING, mapPosition.getBearing())
                .apply();
    }

    public synchronized boolean mapDisplaysNorthUp() {
        return preferences.getBoolean(KEY_MAP_DISPLAY_NORTH_UP, false);
    }

    public synchronized void setMapBoundingBox(BoundingBox boundingBox) {
        preferences.edit()
                .putString(KEY_MAP_BOUNDING_BOX_MIN_LATITUDE, String.valueOf(boundingBox.minLatitude))
                .putString(KEY_MAP_BOUNDING_BOX_MIN_LONGITUDE, String.valueOf(boundingBox.minLongitude))
                .putString(KEY_MAP_BOUNDING_BOX_MAX_LATITUDE, String.valueOf(boundingBox.maxLatitude))
                .putString(KEY_MAP_BOUNDING_BOX_MAX_LONGITUDE, String.valueOf(boundingBox.maxLongitude))
                .apply();
    }

    public synchronized BoundingBox getMapBoundingBox() {
        Double minLat, minLon, maxLat, maxLon;

        minLat = Double.parseDouble(preferences.getString(KEY_MAP_BOUNDING_BOX_MIN_LATITUDE, DEFAULT_LATITUDE));
        minLon = Double.parseDouble(preferences.getString(KEY_MAP_BOUNDING_BOX_MIN_LONGITUDE, DEFAULT_LONGITUDE));
        maxLat = Double.parseDouble(preferences.getString(KEY_MAP_BOUNDING_BOX_MAX_LATITUDE, DEFAULT_LATITUDE));
        maxLon = Double.parseDouble(preferences.getString(KEY_MAP_BOUNDING_BOX_MAX_LONGITUDE, DEFAULT_LONGITUDE));

        return new BoundingBox(minLat, minLon, maxLat, maxLon);
    }

    public synchronized ScaleBarType getScaleBarType() {
        //Stupid ListPreference wants its value as string
        String val = preferences.getString(KEY_MAP_SCALE_BAR_TYPE, ScaleBarType.METRIC_AND_IMPERIAL.name());

        ScaleBarType scaleBarType;

        try {
             scaleBarType = ScaleBarType.valueOf(val);
        } catch (IllegalArgumentException e) {
            scaleBarType = ScaleBarType.METRIC_AND_IMPERIAL;
        }

        return scaleBarType;
    }

    public synchronized boolean getUseTrueNorth() {
        return preferences.getBoolean(KEY_USE_TRUE_NORTH, true);
    }

    public synchronized boolean getUseGpsBearing() {
        return preferences.getBoolean(KEY_USE_GPS_BEARING, false);
    }

    public synchronized int getUseGpsBearingSpeed() {
        return preferences.getInt(KEY_USE_GPS_BEARING_SPEED, 10);
    }

    public synchronized int getUsGpsBearingDuration() {
        return preferences.getInt(KEY_USE_GPS_BEARING_DURATION, 5);
    }

    //TODO: add to preferences.xml and handle
    public synchronized @Units int getUnits() {
        return preferences.getInt(KEY_UNIT, Units.METRIC);
    }
    public synchronized void setUnits(@Units int units) {
        preferences.edit()
                .putInt(KEY_UNIT, units)
                .apply();
    }

    //TODO: add to preferences.xml and handle
    public synchronized @Coordinate.DisplayFormat int getCoordinateDisplayFormat() {
        return preferences.getInt(KEY_COORDINATE_DISPLAY_FORMAT, Coordinate.DisplayFormat.FORMAT_DDMMMMM);
    }

    public synchronized boolean areMapsDownloading() { return preferences.getBoolean(KEY_DOWNLOADING_MAPS, false); }

    public synchronized void setMapsAreDownloading(boolean downloading) {
        preferences.edit()
                .putBoolean(KEY_DOWNLOADING_MAPS, downloading)
                .apply();
    }

    @NonNull
    private List<Long> getLongListForKey(@NonNull String key) {
        List<Long> retryableMapDownloadIds = new ArrayList<>();

        try {
            JSONObject jsonObject = new JSONObject(preferences.getString(key, "{}"));

            if (jsonObject.has(key)) {
                JSONArray jsonArray = jsonObject.getJSONArray(key);

                for (int i=0, length = jsonArray.length(); i < length; i++) {
                    retryableMapDownloadIds.add(jsonArray.getLong(i));
                }
            }
        } catch (JSONException e) {
            //To bad
        }

        return retryableMapDownloadIds;
    }

    private void setLongList(@NonNull String key, @NonNull List<Long> longList) {
        try {
            JSONObject jsonObject = new JSONObject();
            JSONArray jsonArray = new JSONArray();

            for (Long id : longList) {
                jsonArray.put(id);
            }

            jsonObject.put(key, jsonArray);

            preferences.edit().putString(key, jsonObject.toString()).apply();
        } catch (JSONException e) {
            preferences.edit().putString(key, "{}").apply();
        }
    }

    @NonNull
    public synchronized List<Long> getRetryableMapDownloadIds() {
        return getLongListForKey(KEY_RETRYABLE_MAP_DOWNLOAD_IDS);
    }

    public synchronized void setRetryableMapDownloadIds(List<Long> retryableMapDownloadIds) {
        setLongList(KEY_RETRYABLE_MAP_DOWNLOAD_IDS, retryableMapDownloadIds);
    }

    @NonNull
    public synchronized List<Long> getReportedFailedMapDownloadIds() {
        return getLongListForKey(KEY_REPORTED_FAILED_MAP_DOWNLOAD_IDS);
    }

    public synchronized void setReportedFailedMapDownloadIds(List<Long> reportedFailedMapDownloadIds) {
        setLongList(KEY_REPORTED_FAILED_MAP_DOWNLOAD_IDS, reportedFailedMapDownloadIds);
    }

    @NonNull
    private synchronized List<String> getStringList(@NonNull String key) {
        List<String> stringList = new ArrayList<>();

        try {
            JSONObject jsonObject = new JSONObject(preferences.getString(key, "{}"));
            JSONArray jsonArray = jsonObject.getJSONArray(key);

            for (int i = 0, length = jsonArray.length(); i < length; i++) {
                stringList.add(jsonArray.getString(i));
            }
        } catch (JSONException e) {
            //To bad
        }

        return stringList;
    }

    private synchronized void setStringList(@NonNull String key, @NonNull List<String> trackFileIds) {
        try {
            JSONObject jsonObject = new JSONObject();
            JSONArray jsonArray = new JSONArray();

            for (String trackFileId : trackFileIds) {
                jsonArray.put(trackFileId);
            }

            jsonObject.put(key, jsonArray);

            preferences.edit().putString(key, jsonObject.toString()).apply();
        } catch (JSONException e) {
            preferences.edit().putString(key, "{}").apply();
        }
    }

    @NonNull
    public synchronized List<String> getDownloadedTrackIdentifiers() {
        return getStringList(KEY_DOWNLOADED_TRACK_IDENTIFIERS);
    }

    public void setDownloadedTrackIdentifiers(@NonNull List<String> downloadedTrackIdentifiers) {
        setStringList(KEY_DOWNLOADED_TRACK_IDENTIFIERS, downloadedTrackIdentifiers);
    }

    @NonNull
    public synchronized List<String> getDownloadingTrackIdentifiers() {
        return getStringList(KEY_DOWNLOADING_TRACK_IDENTIFIERS);
    }

    public void setDownloadingTrackIdentifiers(@NonNull List<String> downloadingTrackIdentifiers) {
        setStringList(KEY_DOWNLOADING_TRACK_IDENTIFIERS, downloadingTrackIdentifiers);
    }
}
