package nl.erikduisters.pathfinder.ui.fragment.map;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.os.LocaleListCompat;

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
import nl.erikduisters.pathfinder.data.model.map.ScaleBarType;
import nl.erikduisters.pathfinder.util.StringProvider;
import nl.erikduisters.pathfinder.util.menu.MyMenu;
import nl.erikduisters.pathfinder.util.menu.MyMenuItem;
import nl.erikduisters.pathfinder.util.menu.MySubMenu;
import timber.log.Timber;

/**
 * Created by Erik Duisters on 28-06-2018.
 */

/* TODO: Delete
   MapFileTileSource tileSource = new MapFileTileSource();
   //tileSource.setMapFile(preferenceManager.getStorageDir() + preferenceManager.getStorageMapSubDir() + "Netherlands.map");
   //tileSource.setMapFile(preferenceManager.getStorageDir() + preferenceManager.getStorageMapSubDir() + "OpenAndroMaps-Netherlands.map");
   //tileSource.setPreferredLanguage();

   //ExternalRenderTheme theme = new ExternalRenderTheme(preferenceManager.getStorageDir() + preferenceManager.getStorageRenderThemeSubDir() + "Mapsforge/Mapsforge.xml");
   //ExternalRenderTheme theme = new ExternalRenderTheme(preferenceManager.getStorageDir() + preferenceManager.getStorageRenderThemeSubDir() + "Elevate4/Elevate.xml");
   //ExternalRenderTheme theme = new ExternalRenderTheme(preferenceManager.getStorageDir() + preferenceManager.getStorageRenderThemeSubDir() + "Elevate4/Elements.xml");

   //If map is v5 then I can use S3DBLayer
   //When using OSciMap4TileSource with s3db tile source I can then use S3DBTileLayer
*/

//TODO: Map follows gps
//TODO: handle map events (eg. move/zoom in/out)
//TODO: Save MapPosition to preferences
//TODO: MyLocationLayer
//TODO: ScaleBar
@Singleton
public class MapFragmentViewModel extends ViewModel {
    private MutableLiveData<MapInitializationState> mapInitializationStateObservable;
    private MutableLiveData<MapFragmentViewState> viewStateObservable;
    private MutableLiveData<MyMenu> optionsMenuObservable;
    private MutableLiveData<MapPosition> mapPositionObservable;

    private final PreferenceManager preferenceManager;
    private final GpsManager gpsManager;
    private final MyMenu optionsMenu;

    private final MapPosition currentMapPosition;
    private Location previousLocation;

    @Inject
    MapFragmentViewModel(PreferenceManager preferenceManager, GpsManager gpsManager) {
        mapInitializationStateObservable = new MutableLiveData<>();
        viewStateObservable = new MutableLiveData<>();
        optionsMenuObservable = new MutableLiveData<>();
        mapPositionObservable = new MutableLiveData<>();

        optionsMenu = new MyMenu();

        this.preferenceManager = preferenceManager;
        this.gpsManager = gpsManager;

        initOptionsMenu();
        optionsMenuObservable.setValue(optionsMenu);

        currentMapPosition = initCurrentMapPosition();

        previousLocation = new Location(LocationManager.GPS_PROVIDER);
        previousLocation.setLatitude(currentMapPosition.getLatitude());
        previousLocation.setLongitude(currentMapPosition.getLongitude());

        setMapInitializationState();

        mapPositionObservable.setValue(currentMapPosition);

        this.gpsManager.addLocationListener(this::onLocationChanged);
    }

    LiveData<MapInitializationState> getMapInitializationStateObservable() { return mapInitializationStateObservable; }
    LiveData<MapFragmentViewState> getViewStateObservable() { return  viewStateObservable; }
    LiveData<MyMenu> getOptionsMenuObservable() { return optionsMenuObservable; }
    LiveData<MapPosition> getMapPositionObservable() { return mapPositionObservable; }

    private void onLocationChanged(Location location) {
        currentMapPosition.setPosition(location.getLatitude(), location.getLongitude());

        float distance = location.distanceTo(previousLocation);
        double groundResolution = MercatorProjection.groundResolutionWithScale(location.getLatitude(), currentMapPosition.getScale());

        if (distance / groundResolution > 1.0f) {
            mapPositionObservable.setValue(currentMapPosition);
        }

        previousLocation = location;
    }

    private void initOptionsMenu() {
        optionsMenu.add(new MySubMenu(R.id.menu_mapStyle, true, false, true));
    }

    private void setMapInitializationState() {
        TileSource tileSource = getTileSource();
        ThemeFile themeFile = getRenderTheme();

        MapInitializationState.Builder builder = new MapInitializationState.Builder()
                .withTileSource(tileSource)
                .withTheme(themeFile)
                .withBuildingLayer()
                .withLabelLayer()
                .withScaleBarType(ScaleBarType.METRIC_AND_IMPERIAL);    //TODO: Add to settings

        MapInitializationState state = builder.build();

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
        //TODO: use preferences to get map file name
        //TODO: Fall back to online tile source if map is corrupt (Can be detected by return value of OsmTileLayer.setTileSource in MapFragment)
        if (!mapFileTileSource.setMapFile(preferenceManager.getStorageDir() + preferenceManager.getStorageMapSubDir() + "OpenAndroMaps-Netherlands.map")) {
            //TODO: Inform the user
            preferenceManager.setUseOfflineMap(false);
            return getOnlineTileSource();
        }

        //TODO: mapFileTileSource.setPreferredLanguage(ConfigurationCompat.getLocales(getResources().getConfiguration()).get(0).getLanguage());

        return mapFileTileSource;
    }

    private TileSource getOnlineTileSource() {
        return preferenceManager.getOnlineMap().provideTileSource();
    }

    private MapPosition initCurrentMapPosition() {
        //TODO: Tilt?
        MapPosition mapPosition = preferenceManager.getMapPosition();

        if (preferenceManager.mapFollowsGps()) {
            Location lastKnowLocation = gpsManager.getLastKnowLocation();

            if (lastKnowLocation != null) {
                mapPosition.setPosition(lastKnowLocation.getLatitude(), lastKnowLocation.getLongitude());
            }
        }

        return mapPosition;
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

    Set<String> getCategories(XmlRenderThemeStyleMenu styleMenu) {
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
        if (myMenuItem.isChecked()) {
            return;
        }

        preferenceManager.setRenderThemeStyle((String) myMenuItem.getTag());

        MapInitializationState prevState = mapInitializationStateObservable.getValue();
        ThemeFile themeFile = getRenderTheme();

        MapInitializationState.Builder builder = new MapInitializationState.Builder()
                .withTileSource(prevState.tileSource)
                .withTheme(themeFile)
                .withBuildingLayer()
                .withLabelLayer();

        mapInitializationStateObservable.setValue(builder.build());
    }
}
