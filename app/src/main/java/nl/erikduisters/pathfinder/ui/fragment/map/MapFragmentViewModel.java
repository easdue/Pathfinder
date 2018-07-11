package nl.erikduisters.pathfinder.ui.fragment.map;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.os.LocaleListCompat;
import android.view.Menu;

import org.oscim.core.MapPosition;
import org.oscim.core.MercatorProjection;
import org.oscim.theme.IRenderTheme;
import org.oscim.theme.ThemeFile;
import org.oscim.theme.XmlRenderThemeStyleLayer;
import org.oscim.theme.XmlRenderThemeStyleMenu;
import org.oscim.tiling.TileSource;
import org.oscim.tiling.source.mapfile.MapFileTileSource;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import nl.erikduisters.pathfinder.R;
import nl.erikduisters.pathfinder.data.local.GpsManager;
import nl.erikduisters.pathfinder.data.local.PreferenceManager;
import nl.erikduisters.pathfinder.data.model.map.LocationLayerInfo;
import nl.erikduisters.pathfinder.util.StringProvider;
import nl.erikduisters.pathfinder.util.menu.MyMenu;
import nl.erikduisters.pathfinder.util.menu.MyMenuItem;
import nl.erikduisters.pathfinder.util.menu.MySubMenu;
import timber.log.Timber;

/**
 * Created by Erik Duisters on 28-06-2018.
 */

//TODO: Handle gpx fix loss
//TODO: Map orientation (eg. always north/heading)
@Singleton
public class MapFragmentViewModel extends ViewModel {
    private MutableLiveData<MapInitializationState> mapInitializationStateObservable;
    private MutableLiveData<MapFragmentViewState> viewStateObservable;
    private MutableLiveData<MyMenu> optionsMenuObservable;
    private MutableLiveData<MapPosition> mapPositionObservable;
    private MutableLiveData<LocationLayerInfo> locationLayerInfoObservable;

    private final PreferenceManager preferenceManager;
    private final GpsManager gpsManager;
    private MyMenu optionsMenu;

    private MapPosition currentMapPosition;
    private Location previousLocation;

    private final MapInitializationState.Builder mapInitializationStateBuilder;
    private final MapFragmentViewState.Builder mapFragmentViewStateBuilder;

    @Inject
    MapFragmentViewModel(PreferenceManager preferenceManager, GpsManager gpsManager) {
        this.preferenceManager = preferenceManager;
        this.gpsManager = gpsManager;

        initLiveData();

        mapInitializationStateBuilder = new MapInitializationState.Builder();
        mapFragmentViewStateBuilder = new MapFragmentViewState.Builder();

        initOptionsMenu();
        optionsMenuObservable.setValue(optionsMenu);

        initCurrentMapPosition();
        initpreviousLocation();

        setMapInitializationState();

        mapPositionObservable.setValue(currentMapPosition);
        locationLayerInfoObservable.setValue(new LocationLayerInfo());

        this.gpsManager.addLocationListener(this::onLocationChanged);

        updateMapFragmentViewStateForMapFollowsGps(preferenceManager.mapFollowsGps());
    }

    private void initLiveData() {
        mapInitializationStateObservable = new MutableLiveData<>();
        viewStateObservable = new MutableLiveData<>();
        optionsMenuObservable = new MutableLiveData<>();
        mapPositionObservable = new MutableLiveData<>();
        locationLayerInfoObservable = new MutableLiveData<>();
    }

    private void initOptionsMenu() {
        optionsMenu = new MyMenu();
        optionsMenu.add(new MyMenuItem(R.id.menu_mapLockedToGps, true, preferenceManager.mapFollowsGps()));
        optionsMenu.add(new MyMenuItem(R.id.menu_mapNotLockedToGps, true, !preferenceManager.mapFollowsGps()));
        optionsMenu.add(new MySubMenu(R.id.menu_mapStyle, true, false, true));
    }

    private void initCurrentMapPosition() {
        if (currentMapPosition == null) {
            currentMapPosition = preferenceManager.getMapPosition();
        }

        if (preferenceManager.mapFollowsGps()) {
            Location lastKnowLocation = gpsManager.getLastKnowLocation();

            if (lastKnowLocation != null) {
                currentMapPosition.setPosition(lastKnowLocation.getLatitude(), lastKnowLocation.getLongitude());
            }

            currentMapPosition.setBearing(0f);
        }
    }

    private void initpreviousLocation() {
        previousLocation = new Location(LocationManager.GPS_PROVIDER);
        previousLocation.setLatitude(currentMapPosition.getLatitude());
        previousLocation.setLongitude(currentMapPosition.getLongitude());
    }

    LiveData<MapInitializationState> getMapInitializationStateObservable() { return mapInitializationStateObservable; }
    LiveData<MapFragmentViewState> getViewStateObservable() { return  viewStateObservable; }
    LiveData<MyMenu> getOptionsMenuObservable() { return optionsMenuObservable; }
    LiveData<MapPosition> getMapPositionObservable() { return mapPositionObservable; }
    LiveData<LocationLayerInfo> getLocationMarkerInfoObservable() { return locationLayerInfoObservable; }

    private void setMapInitializationState() {
        TileSource tileSource = getTileSource();
        ThemeFile themeFile = getRenderTheme();

        mapInitializationStateBuilder
                .withTileSource(tileSource)
                .withTheme(themeFile)
                .withBuildingLayer()
                .withLabelLayer()
                .withScaleBarType(preferenceManager.getScaleBarType())
                .withLocationLayer()
                .withLocationMarker(R.raw.ic_map_location_marker);

        MapInitializationState state = mapInitializationStateBuilder.build();

        mapInitializationStateObservable.setValue(state);
    }

    private TileSource getTileSource() {
        TileSource tileSource;

        if (preferenceManager.useOfflineMap()) {
            tileSource = getOfflineTileSource();
        } else {
            tileSource = getOnlineTileSource();
        }

        return tileSource;
    }

    private TileSource getOfflineTileSource() {
        MapFileTileSource mapFileTileSource = new MapFileTileSource();

        if (!mapFileTileSource.setMapFile(preferenceManager.getStorageDir() + preferenceManager.getStorageMapSubDir() + preferenceManager.getOfflineMap())) {
            //TODO: Inform the user that the mapfile does not exits
            preferenceManager.setUseOfflineMap(false);
            return getOnlineTileSource();
        }

        //TODO: mapFileTileSource.setPreferredLanguage();

        return mapFileTileSource;
    }

    private TileSource getOnlineTileSource() {
        return preferenceManager.getOnlineMap().provideTileSource();
    }

    void tileSourceCannotBeSet(TileSource tileSource) {
        if (tileSource instanceof MapFileTileSource) {
            preferenceManager.setUseOfflineMap(false);

            MapInitializationState state = mapInitializationStateBuilder
                    .withTileSource(getOnlineTileSource())
                    .build();

            mapInitializationStateObservable.setValue(state);

            //TODO: Inform the user that the mapfile is corrupted or not a mapsforge map
        }
    }

    private ThemeFile getRenderTheme() {
        ThemeFile themeFile;

        if (preferenceManager.useExternalRenderTheme()) {
            try {
                themeFile = preferenceManager.getExternalRenderTheme();
                themeFile.setMenuCallback(this::getCategories);
            } catch (IRenderTheme.ThemeException e) {
                //TODO: Inform user
                themeFile = preferenceManager.getInternalRenderTheme();
            }
        } else {
            themeFile = preferenceManager.getInternalRenderTheme();
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
        optionsMenuObservable.setValue(optionsMenu);
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

        ThemeFile themeFile = getRenderTheme();

        mapInitializationStateBuilder
                .withTheme(themeFile);

        mapInitializationStateObservable.setValue(mapInitializationStateBuilder.build());
    }

    void onMapLongPress() {
        boolean followGps = !preferenceManager.mapFollowsGps();

        preferenceManager.setMapFollowsGps(followGps);

        MyMenuItem myMenuItem = optionsMenu.findItem(R.id.menu_mapLockedToGps);
        myMenuItem.setVisible(followGps);
        myMenuItem = optionsMenu.findItem(R.id.menu_mapNotLockedToGps);
        myMenuItem.setVisible(!followGps);

        optionsMenuObservable.setValue(optionsMenu);

        updateMapFragmentViewStateForMapFollowsGps(followGps);
    }

    private void updateMapFragmentViewStateForMapFollowsGps(boolean followGps) {
        MapFragmentViewState state = mapFragmentViewStateBuilder
                .withMoveEnabled(!followGps)
                .withRotationEnabled(!followGps)
                .withTiltEnabled(true)
                .withZoomEnabled(true)
                .build();

        viewStateObservable.setValue(state);

        initCurrentMapPosition();
        mapPositionObservable.setValue(currentMapPosition);
    }

    void onMapPositionChangedByUser(MapPosition mapPosition) {
        Timber.e("onMapPositionChangedByUser");

        currentMapPosition.copy(mapPosition);
    }

    void onSaveState() {
        Timber.e("onSaveState()");

        preferenceManager.setMapPosition(currentMapPosition);
    }

    private void onLocationChanged(Location location) {
        if (!preferenceManager.mapFollowsGps()) {
            return;
        }

        currentMapPosition.setPosition(location.getLatitude(), location.getLongitude());

        float distance = location.distanceTo(previousLocation);
        double groundResolution = MercatorProjection.groundResolutionWithScale(location.getLatitude(), currentMapPosition.getScale());

        if (distance / groundResolution > 1.0f) {
            mapPositionObservable.setValue(currentMapPosition);
        }

        locationLayerInfoObservable.setValue(new LocationLayerInfo(location));

        previousLocation = location;
    }
}
