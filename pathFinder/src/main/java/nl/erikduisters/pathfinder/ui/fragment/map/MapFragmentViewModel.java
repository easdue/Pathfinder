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

package nl.erikduisters.pathfinder.ui.fragment.map;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.content.SharedPreferences;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.os.LocaleListCompat;
import android.view.Menu;

import org.oscim.core.Box;
import org.oscim.core.MapPosition;
import org.oscim.map.Viewport;
import org.oscim.theme.IRenderTheme;
import org.oscim.theme.ThemeFile;
import org.oscim.theme.XmlRenderThemeStyleLayer;
import org.oscim.theme.XmlRenderThemeStyleMenu;
import org.oscim.tiling.TileSource;
import org.oscim.tiling.source.bitmap.BitmapTileSource;
import org.oscim.tiling.source.mapfile.MapFileTileSource;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import nl.erikduisters.pathfinder.R;
import nl.erikduisters.pathfinder.async.BackgroundJobHandler;
import nl.erikduisters.pathfinder.async.UseCaseJob;
import nl.erikduisters.pathfinder.data.local.ExternalRenderThemeManager;
import nl.erikduisters.pathfinder.data.local.GpsManager;
import nl.erikduisters.pathfinder.data.local.HeadingManager;
import nl.erikduisters.pathfinder.data.local.MinimalTrackListLoader;
import nl.erikduisters.pathfinder.data.local.PreferenceManager;
import nl.erikduisters.pathfinder.data.local.SelectedTrackLoader;
import nl.erikduisters.pathfinder.data.model.FullTrack;
import nl.erikduisters.pathfinder.data.model.MinimalTrack;
import nl.erikduisters.pathfinder.data.model.MinimalTrackList;
import nl.erikduisters.pathfinder.data.usecase.LoadRenderTheme;
import nl.erikduisters.pathfinder.data.usecase.UseCase;
import nl.erikduisters.pathfinder.ui.fragment.map.MapInitializationState.MapInitializedState;
import nl.erikduisters.pathfinder.util.BoundingBox;
import nl.erikduisters.pathfinder.util.IntegerDegrees;
import nl.erikduisters.pathfinder.util.SingleSourceMediatorLiveData;
import nl.erikduisters.pathfinder.util.StringProvider;
import nl.erikduisters.pathfinder.util.map.LocationLayerInfo;
import nl.erikduisters.pathfinder.util.menu.MyMenu;
import nl.erikduisters.pathfinder.util.menu.MyMenuItem;
import nl.erikduisters.pathfinder.util.menu.MySubMenu;
import okhttp3.OkHttpClient;
import timber.log.Timber;

import static nl.erikduisters.pathfinder.ui.fragment.map.MapInitializationState.MapInitializingState;

/**
 * Created by Erik Duisters on 28-06-2018.
 */
//TODO: Show currently recording track
@Singleton
public class MapFragmentViewModel
        extends ViewModel
        implements GpsManager.GpsFixListener, HeadingManager.HeadingListener,
                   SharedPreferences.OnSharedPreferenceChangeListener, GpsManager.LocationListener, SelectedTrackLoader.Listener {
    private MutableLiveData<MapInitializationState> mapInitializationStateObservable;
    private SingleSourceMediatorLiveData<MapFragmentViewState> mapFragmentViewStateObservable;

    private final PreferenceManager preferenceManager;
    private final GpsManager gpsManager;
    private final BackgroundJobHandler backgroundJobHandler;
    private final HeadingManager headingManager;
    private final OkHttpClient okHttpClient;
    private final ExternalRenderThemeManager externalRenderThemeManager;
    private final MinimalTrackListLoader minimalTrackListLoader;
    private final SelectedTrackLoader selectedTrackLoader;

    private MapInitializedState.Builder mapInitializedStateBuilder;
    private MapFragmentViewState.Builder mapFragmentViewStateBuilder;

    private UseCaseJob renderThemeJob;
    @Nullable private Viewport viewport;
    private long selectedTrackId;

    @Inject
    MapFragmentViewModel(PreferenceManager preferenceManager,
                         GpsManager gpsManager,
                         BackgroundJobHandler backgroundJobHandler,
                         HeadingManager headingManager,
                         OkHttpClient okHttpClient,
                         ExternalRenderThemeManager externalRenderThemeManager,
                         MinimalTrackListLoader minimalTrackListLoader,
                         SelectedTrackLoader selectedTrackLoader) {
        this.preferenceManager = preferenceManager;
        this.gpsManager = gpsManager;
        this.backgroundJobHandler = backgroundJobHandler;
        this.headingManager = headingManager;
        this.okHttpClient = okHttpClient;
        this.externalRenderThemeManager = externalRenderThemeManager;
        this.minimalTrackListLoader = minimalTrackListLoader;
        this.selectedTrackLoader = selectedTrackLoader;

        this.selectedTrackLoader.addListener(this);

        initLiveData();
        initMapFragmentViewStateBuilder();
        initMapInitializedStateBuilder();

        preferenceManager.registerOnSharedPreferenceChangeListener(this);

        selectedTrackId = 0;
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        preferenceManager.unregisterOnSharedPreferenceChangeListener(this);
        selectedTrackLoader.removeListener(this);
        selectedTrackId = 0;
    }

    LiveData<MapInitializationState> getMapInitializationStateObservable() { return mapInitializationStateObservable; }
    LiveData<MapFragmentViewState> getMapFragmentViewStateObservable() { return mapFragmentViewStateObservable; }

    private void initLiveData() {
        mapInitializationStateObservable = new MutableLiveData<>();
        mapFragmentViewStateObservable = new SingleSourceMediatorLiveData<>();
        mapFragmentViewStateObservable.addSource(minimalTrackListLoader.getMinimalTrackListObservable(), this::minimalTrackListToViewState);
    }

    private void minimalTrackListToViewState(MinimalTrackList minimalTrackList) {
        mapFragmentViewStateBuilder.withMinimalTrackList(minimalTrackList);

        setMapFragmentViewStateIfInitialized();
    }

    private void initMapFragmentViewStateBuilder() {
        mapFragmentViewStateBuilder = new MapFragmentViewState.Builder();

        initOptionsMenu();
        initMapPosition();
        initLocationLayerInfo();
    }

    private void initOptionsMenu() {
        MyMenu optionsMenu = new MyMenu();
        optionsMenu.add(new MyMenuItem(R.id.menu_mapLockedToGps, true, preferenceManager.mapFollowsGps()));
        optionsMenu.add(new MyMenuItem(R.id.menu_mapNotLockedToGps, true, !preferenceManager.mapFollowsGps()));
        optionsMenu.add(new MySubMenu(R.id.menu_mapStyle, true, false, true));

        mapFragmentViewStateBuilder.withOptionsMenu(optionsMenu);
    }

    private void initMapPosition() {
        MapPosition mapPosition;

        mapPosition = preferenceManager.getMapPosition();

        if (preferenceManager.mapFollowsGps()) {
            Location lastKnowLocation = gpsManager.getLastKnowLocation();

            mapPosition.setPosition(lastKnowLocation.getLatitude(), lastKnowLocation.getLongitude());
        }

        mapPosition.setBearing(0);

        mapFragmentViewStateBuilder.withMapPosition(mapPosition);
    }

    private void initLocationLayerInfo() {
        LocationLayerInfo locationLayerInfo = new LocationLayerInfo(gpsManager.getLastKnowLocation());

        mapFragmentViewStateBuilder.withLocationLayerInfo(locationLayerInfo);
    }

    private void initMapInitializedStateBuilder() {
        mapInitializedStateBuilder = new MapInitializedState.Builder();

        mapInitializedStateBuilder
                .withTileSource(getTileSource())
                .withBuildingLayer()
                .withLabelLayer()
                .withScaleBarType(preferenceManager.getScaleBarType())
                .withLocationLayer()
                .withLocationFixedMarker(R.raw.ic_map_location_marker_fix)
                .withLocationNotFixedMarker(R.raw.ic_map_location_marker_no_fix);
    }

    private TileSource getTileSource() {
        TileSource tileSource;

        if (preferenceManager.useOnlineMap()) {
            tileSource = getOnlineTileSource();
        } else {
            tileSource = getOfflineTileSource();
        }

        updateMapFragmentViewStateFor(tileSource);

        return tileSource;
    }

    private TileSource getOfflineTileSource() {
        MapFileTileSource mapFileTileSource = new MapFileTileSource();

        File mapFile = new File(preferenceManager.getStorageMapDir(), preferenceManager.getOfflineMap());

        if (!mapFileTileSource.setMapFile(mapFile.getAbsolutePath())) {
            //TODO: Inform the user that the mapfile does not exits
            preferenceManager.setUseOnlineMap(true);
            preferenceManager.setOfflineMap("");
            return getOnlineTileSource();
        }

        //TODO: mapFileTileSource.setPreferredLanguage();

        return mapFileTileSource;
    }

    private TileSource getOnlineTileSource() {
        return preferenceManager.getOnlineMap().provideTileSource(okHttpClient.newBuilder());
    }

    private void updateMapFragmentViewStateFor(TileSource tileSource) {
        if (tileSource instanceof BitmapTileSource) {
            setMapStyleMenuVisibility(false);
        } else if (mapInitializedStateBuilder.hasRenderTheme()) {
            setMapStyleMenuVisibility(true);
        }
    }

    void tileSourceCannotBeSet(TileSource tileSource) {
        if (tileSource instanceof MapFileTileSource) {
            preferenceManager.setUseOnlineMap(true);
            preferenceManager.setOfflineMap("");

            clearMapStyleMenu();

            MapInitializationState state = mapInitializedStateBuilder
                    .withTileSource(getOnlineTileSource())
                    .build();

            mapInitializationStateObservable.setValue(state);

            //TODO: Inform the user that the mapfile is corrupted or not a mapsforge map
        }
    }

    void onMapViewReady(Viewport viewport) {
        this.viewport = viewport;
        loadRenderTheme();
    }

    void releaseViewPort() {
        this.viewport = null;
    }

    private void loadRenderTheme() {
        cancelPreviousRenderJob();

        MapInitializingState state = new MapInitializingState(R.string.loading_render_theme);
        mapInitializationStateObservable.setValue(state);
        clearMapStyleMenu();
        mapFragmentViewStateObservable.setValue(null);

        ThemeFile themeFile = getRenderTheme();

        LoadRenderTheme useCase = new LoadRenderTheme(themeFile, new UseCase.Callback<IRenderTheme>() {
            @Override
            public void onResult(@Nullable IRenderTheme result) {
                setMapInitializedState(result);
                renderThemeJob = null;
            }

            @Override
            public void onError(@NonNull Throwable error) {
                renderThemeJob = null;

                if (!preferenceManager.useInternalRenderTheme()) {
                    preferenceManager.setUseInternalRenderTheme(true);
                    clearMapStyleMenu();
                    loadRenderTheme();
                } // else assets have become corrupt?
            }
        });

        backgroundJobHandler.runJob(useCase.getUseCaseJob());
    }

    private void cancelPreviousRenderJob() {
        if (renderThemeJob != null && backgroundJobHandler.isRunning(renderThemeJob)) {
            backgroundJobHandler.cancelJob(renderThemeJob);
            renderThemeJob = null;
        }
    }

    private void clearMapStyleMenu() {
        MyMenu optionsMenu = new MyMenu(mapFragmentViewStateBuilder.getOptionsMenu());
        MyMenuItem mapStyleMenu = optionsMenu.findItem(R.id.menu_mapStyle);

        ((MySubMenu) mapStyleMenu).getSubMenu().getMenuItems().clear();
        mapStyleMenu.setVisible(false);

        mapFragmentViewStateBuilder
                .withOptionsMenu(optionsMenu);
    }

    private void setMapStyleMenuVisibility(boolean visible) {
        MyMenu optionsMenu = new MyMenu(mapFragmentViewStateBuilder.getOptionsMenu());
        MyMenuItem mapStyleMenu = optionsMenu.findItem(R.id.menu_mapStyle);
        mapStyleMenu.setVisible(visible);

        mapFragmentViewStateBuilder
                .withOptionsMenu(optionsMenu);
    }

    private void setMapInitializedState(IRenderTheme renderTheme) {
        MapInitializedState state = mapInitializedStateBuilder
                .withRenderTheme(renderTheme)
                .build();

        mapInitializationStateObservable.setValue(state);
        updateMapFragmentViewState(preferenceManager.mapFollowsGps());

        mapFragmentViewStateObservable.setValue(mapFragmentViewStateBuilder.build());
    }

    private ThemeFile getRenderTheme() {
        ThemeFile themeFile;

        if (preferenceManager.useInternalRenderTheme()) {
            themeFile = preferenceManager.getInternalRenderTheme();
        } else {
            try {
                themeFile = externalRenderThemeManager.getThemeWithName(preferenceManager.getExternalRenderThemeName());
                themeFile.setMenuCallback(this::getCategories);
            } catch (IRenderTheme.ThemeException e) {
                //TODO: Inform user
                preferenceManager.setUseInternalRenderTheme(true);
                themeFile = preferenceManager.getInternalRenderTheme();
                clearMapStyleMenu();
            }
        }

        return themeFile;
    }

    private Set<String> getCategories(XmlRenderThemeStyleMenu styleMenu) {
        Map<String, Map<String, String>> renderStyles = new LinkedHashMap<>();
        Set<String> categories = null;

        String themeStyle = preferenceManager.getRenderThemeStyle();

        if (themeStyle.isEmpty() || !styleMenuHasStyle(styleMenu, themeStyle)) {
            themeStyle = styleMenu.getDefaultValue();
            preferenceManager.setRenderThemeStyle(themeStyle);
        }

        for (XmlRenderThemeStyleLayer layer : styleMenu.getLayers().values()) {
            if (layer.isVisible()) {
                renderStyles.put(layer.getId(), layer.getTitles());

                if (layer.getId().equals(themeStyle)) {
                    categories = layer.getCategories();

                    for (XmlRenderThemeStyleLayer overlay : layer.getOverlays()) {
                        if (overlay.isEnabled()) {
                            categories.addAll(overlay.getCategories());
                        }
                    }
                }
            }
        }

        handleAvailableRenderStyles(renderStyles, themeStyle);

        Timber.d("Selected Categories: %s", categories);

        return categories;
    }

    private boolean styleMenuHasStyle(XmlRenderThemeStyleMenu styleMenu, String styleId) {
        for (XmlRenderThemeStyleLayer layer : styleMenu.getLayers().values()) {
            if (layer.isVisible() && layer.getId().equals(styleId)) {
                return true;
            }
        }

        return false;
    }

    private void handleAvailableRenderStyles(Map<String, Map<String, String>> renderStyles, String currentStyle) {
        MyMenu optionsMenu = new MyMenu(mapFragmentViewStateBuilder.getOptionsMenu());
        MyMenuItem menuItem = optionsMenu.findItem(R.id.menu_mapStyle);

        if (menuItem == null) {
            return;
        }

        MyMenu styleMenu = ((MySubMenu) menuItem).getSubMenu();
        styleMenu.getMenuItems().clear();

        for (Map.Entry<String, Map<String, String>> entry : renderStyles.entrySet()) {
            String styleId = entry.getKey();
            Map<String, String> availableLanguages = entry.getValue();

            String styleName = getStyleName(availableLanguages);

            StringProvider title = new StringProvider(styleName);

            MyMenuItem styleItem = new MyMenuItem(true, true, title);
            styleItem.setChecked(styleId.equals(currentStyle));
            styleItem.setTab(styleId);

            styleMenu.add(styleItem);
        }

        menuItem.setVisible(true);

        mapFragmentViewStateBuilder
                .withOptionsMenu(optionsMenu);
    }

    private String getStyleName(Map<String, String> availableLanguages) {
        LocaleListCompat localeList = LocaleListCompat.getDefault();

        String styleName = null;

        for (int i = 0; i < localeList.size(); i++) {
            String language = localeList.get(i).getLanguage();

            if (availableLanguages.containsKey(language)) {
                styleName = availableLanguages.get(language);
                break;
            }
        }

        if (styleName == null ) {
            styleName = availableLanguages.containsKey("en")
                    ? availableLanguages.get("en") : availableLanguages.entrySet().iterator().next().getValue();
        }

        return styleName;
    }

    void onMenuItemSelected(MyMenuItem myMenuItem) {
        switch (myMenuItem.getId()) {
            case Menu.NONE:
                if (!myMenuItem.isChecked()) {
                    changeRenderThemeStyle((String) myMenuItem.getTag());
                }
                break;
            case R.id.menu_mapLockedToGps:
                onMapLongPress();
                break;
            case R.id.menu_mapNotLockedToGps:
                onMapLongPress();
                break;
        }

    }

    private void changeRenderThemeStyle(String renderThemeStyle) {
        preferenceManager.setRenderThemeStyle(renderThemeStyle);

        loadRenderTheme();
    }

    void onMapLongPress() {
        boolean followGps = !preferenceManager.mapFollowsGps();

        preferenceManager.setMapFollowsGps(followGps);

        MyMenu optionsMenu = new MyMenu(mapFragmentViewStateBuilder.getOptionsMenu());
        MyMenuItem myMenuItem = optionsMenu.findItem(R.id.menu_mapLockedToGps);

        //noinspection ConstantConditions
        myMenuItem.setVisible(followGps);
        myMenuItem = optionsMenu.findItem(R.id.menu_mapNotLockedToGps);
        //noinspection ConstantConditions
        myMenuItem.setVisible(!followGps);

        mapFragmentViewStateBuilder
                .withOptionsMenu(optionsMenu);

        updateMapFragmentViewState(followGps);

        if (followGps) {
            Location lastKnownLocation = gpsManager.getLastKnowLocation();
            onLocationChanged(lastKnownLocation);
            minimalTrackListLoader.onLocationChanged(lastKnownLocation);
        } else {
            mapFragmentViewStateObservable.setValue(mapFragmentViewStateBuilder.build());
        }
    }

    private void updateMapFragmentViewState(boolean followGps) {
        mapFragmentViewStateBuilder
                .withMoveEnabled(!followGps)
                .withRotationEnabled(!followGps)
                .withTiltEnabled(true)
                .withZoomEnabled(true)
                .build();
    }

    void onMapPositionChangedByUser(MapPosition mapPosition) {
        mapFragmentViewStateBuilder.getMapPosition().copy(mapPosition);

        preferenceManager.setMapPosition(mapPosition);

        Location location = new Location("map");
        location.setLatitude(mapPosition.getLatitude());
        location.setLongitude(mapPosition.getLongitude());

        minimalTrackListLoader.onMapLocationChanged(location);
    }

    void onSaveState() {
        preferenceManager.setMapPosition(mapFragmentViewStateBuilder.getMapPosition());
        if (viewport != null) {
            Box box = viewport.getBBox(null, 0);

            preferenceManager.setMapBoundingBox(new BoundingBox(box));
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (!preferenceManager.mapFollowsGps()) {
            return;
        }

        MapPosition mapPosition = mapFragmentViewStateBuilder.getMapPosition();

        mapPosition.setPosition(location.getLatitude(), location.getLongitude());

        LocationLayerInfo locationLayerInfo = mapFragmentViewStateBuilder.getLocationLayerInfo();

        locationLayerInfo.latitude = location.getLatitude();
        locationLayerInfo.longitude = location.getLongitude();
        locationLayerInfo.hasFix = true;

        mapFragmentViewStateBuilder
                .withMapPosition(mapPosition)
                .withLocationLayerInfo(locationLayerInfo);

        setMapFragmentViewStateIfInitialized();
    }

    @Override
    public void onGpsFixAcquired() {
        handleFixChange(true);
    }

    @Override
    public void onGpsFixLost() {
        handleFixChange(false);
    }

    private void handleFixChange(boolean hasFix) {
        LocationLayerInfo locationLayerInfo = mapFragmentViewStateBuilder.getLocationLayerInfo();
        locationLayerInfo.hasFix = hasFix;

        mapFragmentViewStateBuilder
                .withLocationLayerInfo(locationLayerInfo);

        setMapFragmentViewStateIfInitialized();
    }

    void onVisible() {
        if (!preferenceManager.mapDisplaysNorthUp()) {
            headingManager.addHeadingListener(this);
        }

        gpsManager.addLocationListener(this);
        gpsManager.addGpsFixListener(this);
    }

    void onInvisible() {
        headingManager.removeHeadingListener(this);

        gpsManager.removeLocationListener(this);
        gpsManager.removeGpsFixListener(this);
    }

    @Override
    public void onHeadingChanged(IntegerDegrees heading) {
        MapPosition mapPosition = mapFragmentViewStateBuilder.getMapPosition();
        LocationLayerInfo locationLayerInfo = mapFragmentViewStateBuilder.getLocationLayerInfo();

        if (preferenceManager.mapDisplaysNorthUp()) {
            if (mapPosition.getBearing() != 0f) {
                mapPosition.setBearing(0f);
            }

            locationLayerInfo.hasBearing = true;
            locationLayerInfo.bearing = -heading.get();
        } else {
            mapPosition.setBearing(-heading.get());

            locationLayerInfo.hasBearing = true;
            locationLayerInfo.bearing = -heading.get();
        }

        setMapFragmentViewStateIfInitialized();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (handleTileSourcePreferenceChanges(key)) {
            return;
        }

        if (handleRenderThemePreferenceChanges(key)) {
            return;
        }

        if (preferenceManager.KEY_MAP_SCALE_BAR_TYPE.equals(key)) {
            mapInitializedStateBuilder.withScaleBarType(preferenceManager.getScaleBarType());

            setMapInitializedStateIfRenderThemeIsSet();
        }

        if (preferenceManager.KEY_MAP_DISPLAY_NORTH_UP.equals(key)) {
            if (preferenceManager.mapDisplaysNorthUp()) {
                headingManager.removeHeadingListener(this);
                onHeadingChanged(new IntegerDegrees(0));
            } else {
                headingManager.addHeadingListener(this);
            }
        }
    }

    private boolean handleTileSourcePreferenceChanges(String key) {
        boolean useOnlineMapPrefChanged = preferenceManager.KEY_USE_ONLINE_MAP.equals(key);
        boolean onlineMapPrefChanged = preferenceManager.KEY_ONLINE_MAP.equals(key);
        boolean offLineMapPrefChanged = preferenceManager.KEY_OFFLINE_MAP.equals(key);

        boolean setNewTileSource = false;

        if (onlineMapPrefChanged && preferenceManager.useOnlineMap()) {
            setNewTileSource = true;
        } else if (offLineMapPrefChanged && !preferenceManager.useOnlineMap()) {
            setNewTileSource = true;
        } else if (useOnlineMapPrefChanged) {
            setNewTileSource = true;
        }

        if (setNewTileSource) {
            mapInitializedStateBuilder.withTileSource(getTileSource());

            setMapInitializedStateIfRenderThemeIsSet();
        }

        return useOnlineMapPrefChanged || onlineMapPrefChanged || offLineMapPrefChanged;
    }

    private boolean handleRenderThemePreferenceChanges(String key) {
        boolean useInternalRenderThemePrefChanged = preferenceManager.KEY_USE_INTERNAL_RENDER_THEME.equals(key);
        boolean internalRenderThemePrefChanged = preferenceManager.KEY_INTERNAL_RENDER_THEME.equals(key);
        boolean externalRenderThemePrefChanged = preferenceManager.KEY_EXTERNAL_RENDER_THEME.equals(key);

        boolean loadNewRenderTheme = false;

        if (internalRenderThemePrefChanged && preferenceManager.useInternalRenderTheme()) {
            loadNewRenderTheme = true;
        } else if (externalRenderThemePrefChanged && !preferenceManager.useInternalRenderTheme()) {
            loadNewRenderTheme = true;
        } else if (useInternalRenderThemePrefChanged) {
            loadNewRenderTheme = true;
        }

        if (loadNewRenderTheme) {
            loadRenderTheme();
        }

        return useInternalRenderThemePrefChanged || internalRenderThemePrefChanged || externalRenderThemePrefChanged;
    }

    private void setMapInitializedStateIfRenderThemeIsSet() {
        if (mapInitializedStateBuilder.hasRenderTheme()) {
            setMapInitializedState(mapInitializedStateBuilder.getRenderTheme());
        }
    }

    private void setMapFragmentViewStateIfInitialized() {
        if (mapInitializationStateObservable.getValue() instanceof MapInitializedState) {
            mapFragmentViewStateObservable.setValue(mapFragmentViewStateBuilder.build());
        }
    }

    @Override
    public void onFullTrackLoaded(@Nullable FullTrack fullTrack) {
        selectedTrackId = fullTrack.id;

        mapFragmentViewStateBuilder.withFullTrack(fullTrack);

        setMapFragmentViewStateIfInitialized();
    }

    void onMinimalTrackClicked(MinimalTrack minimalTrack) {
        if (selectedTrackId != minimalTrack.id) {
            selectedTrackLoader.loadTrackWithId(minimalTrack.id);
        }
    }
}
