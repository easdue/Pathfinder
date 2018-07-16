package nl.erikduisters.pathfinder.ui.fragment.map;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RawRes;
import android.support.annotation.StringRes;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.oscim.android.MapView;
import org.oscim.backend.CanvasAdapter;
import org.oscim.backend.canvas.Bitmap;
import org.oscim.core.MapPosition;
import org.oscim.event.Event;
import org.oscim.event.Gesture;
import org.oscim.event.GestureListener;
import org.oscim.event.MotionEvent;
import org.oscim.layers.AbstractMapEventLayer;
import org.oscim.layers.Layer;
import org.oscim.layers.LocationTextureLayer;
import org.oscim.layers.tile.buildings.BuildingLayer;
import org.oscim.layers.tile.vector.OsmTileLayer;
import org.oscim.layers.tile.vector.labeling.LabelLayer;
import org.oscim.map.Layers;
import org.oscim.map.Map;
import org.oscim.renderer.BitmapRenderer;
import org.oscim.renderer.GLViewport;
import org.oscim.renderer.atlas.TextureAtlas;
import org.oscim.renderer.atlas.TextureRegion;
import org.oscim.scalebar.DefaultMapScaleBar;
import org.oscim.scalebar.ImperialUnitAdapter;
import org.oscim.scalebar.MapScaleBarLayer;
import org.oscim.scalebar.MetricUnitAdapter;
import org.oscim.scalebar.NauticalUnitAdapter;
import org.oscim.utils.IOUtils;
import org.oscim.utils.TextureAtlasUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import butterknife.BindView;
import nl.erikduisters.pathfinder.R;
import nl.erikduisters.pathfinder.data.model.map.LocationLayerInfo;
import nl.erikduisters.pathfinder.data.model.map.ScaleBarType;
import nl.erikduisters.pathfinder.ui.BaseFragment;
import nl.erikduisters.pathfinder.ui.activity.ViewPagerFragment;
import nl.erikduisters.pathfinder.ui.fragment.map.MapInitializationState.MapInitializedState;
import nl.erikduisters.pathfinder.ui.fragment.map.MapInitializationState.MapInitializingState;
import nl.erikduisters.pathfinder.util.menu.MyMenu;
import nl.erikduisters.pathfinder.util.menu.MyMenuItem;

/**
 * Created by Erik Duisters on 28-06-2018.
 */

public class MapFragment
        extends BaseFragment<MapFragmentViewModel>
        implements Map.UpdateListener, ViewPagerFragment {
    @BindView(R.id.mapView) MapView mapView;
    @BindView(R.id.progressGroup) View progressGroup;
    @BindView(R.id.progressMessage) TextView progressMessage;

    private Map map;
    private MapFragmentViewState currentMapFragmentViewState;
    @NonNull private MyMenu optionsMenu;
    private LocationTextureLayer locationTextureLayer;
    private DefaultMapScaleBar scaleBar;
    private TextureRegion locationFixedRegion;
    private TextureRegion locationNotFixedRegion;

    public static MapFragment newInstance() {
        return new MapFragment();
    }

    public MapFragment() {
        optionsMenu = new MyMenu();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);

        map = mapView.map();
        map.getEventLayer().setFixOnCenter(true);
        map.events.bind(this);

        viewModel.onMapViewReady();
        viewModel.getMapInitializationStateObservable().observe(this, this::render);
        viewModel.getMapFragmentViewStateObservable().observe(this, this::render);

        return v;
    }

    @Override
    public void onDestroyView() {
        map.events.unbind(this);

        //If I don't call this MainActivity is sometimes leaked
        if (locationTextureLayer != null) {
            locationTextureLayer.setEnabled(false);
        }

        if (scaleBar != null) {
            scaleBar.destroy();
        }

        mapView.onDestroy();

        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();

        mapView.onResume();
    }

    @Override
    public void onPause() {
        mapView.onPause();

        super.onPause();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        viewModel.onSaveState();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_map;
    }

    @Override
    protected Class<MapFragmentViewModel> getViewModelClass() {
        return MapFragmentViewModel.class;
    }

    private void render(@Nullable MapFragmentViewState viewState) {
        if (viewState == null) {
            currentMapFragmentViewState = null;
            return;
        }

        if (currentMapFragmentViewState == null || currentMapFragmentViewState.optionsMenu != viewState.optionsMenu) {
            this.optionsMenu = viewState.optionsMenu;
            invalidateOptionsMenu();
        }

        if (currentMapFragmentViewState == null || currentMapFragmentViewState.locationLayerInfo != viewState.locationLayerInfo) {
            handleLocationLayerInfo(viewState.locationLayerInfo);
        }

        if (currentMapFragmentViewState == null || currentMapFragmentViewState.mapPosition != viewState.mapPosition) {
            map.setMapPosition(viewState.mapPosition);
        }

        AbstractMapEventLayer eventLayer = map.getEventLayer();

        eventLayer.enableMove(viewState.moveEnabled);
        eventLayer.enableRotation(viewState.rotationEnabled);
        eventLayer.enableTilt(viewState.tiltEnabled);
        eventLayer.enableZoom(viewState.zoomEnabled);

        currentMapFragmentViewState = viewState;
    }

    private void handleLocationLayerInfo(LocationLayerInfo info) {
        if (info.hasFix) {
            locationTextureLayer.locationRenderer.setTextureRegion(locationFixedRegion);
            locationTextureLayer.setPosition(info.latitude, info.longitude, info.bearing, info.accuracy);
        } else {
            locationTextureLayer.locationRenderer.setTextureRegion(locationNotFixedRegion);
            locationTextureLayer.setPosition(info.latitude, info.longitude, info.bearing, 0f);
        }
    }

    private void render(@Nullable MapInitializationState viewState) {
        if (viewState == null) {
            return;
        }

        if (viewState instanceof MapInitializingState) {
            showProgress(((MapInitializingState) viewState).progressMessageResId);
        } else {
            showMap();
        }

        if (viewState instanceof MapInitializedState) {
            render((MapInitializedState) viewState);
        }
    }

    private void showProgress(@StringRes int progressMessageResId) {
        progressGroup.setVisibility(View.VISIBLE);
        progressMessage.setText(progressMessageResId);
    }

    private void showMap() {
        progressGroup.setVisibility(View.GONE);
    }

    private void render(MapInitializedState state) {
        Layers layers = map.layers();

        for (int i = layers.size() - 1; i >= 1; i--) {
            layers.remove(i);
        }

        OsmTileLayer tileLayer = new OsmTileLayer(map);

        if (!tileLayer.setTileSource(state.tileSource)) {
            viewModel.tileSourceCannotBeSet(state.tileSource);
            return;
        }

        map.setBaseMap(tileLayer);

        layers.add(new GestureLayer(map));

        if (state.addBuildingLayer) {
            layers.add(new BuildingLayer(map, tileLayer));
        }

        if (state.addLabelLayer) {
            layers.add(new LabelLayer(map, tileLayer));
        }

        addScaleBarLayer(state.scaleBarType);

        if (state.addLocationLayer) {
            addLocationLayer(state.locationFixedMarkerSvgResId, state.locationNotFixedMarkerSvgResId);
        }

        map.setTheme(state.renderTheme);
    }

    private void addScaleBarLayer(@ScaleBarType int scaleBarType) {
        if (scaleBarType == ScaleBarType.NONE) {
            return;
        }

        if (scaleBar == null) {
            scaleBar = new DefaultMapScaleBar(map);
        }

        switch (scaleBarType) {
            case ScaleBarType.METRIC:
                scaleBar.setScaleBarMode(DefaultMapScaleBar.ScaleBarMode.SINGLE);
                scaleBar.setDistanceUnitAdapter(MetricUnitAdapter.INSTANCE);
                break;
            case ScaleBarType.IMPERIAL:
                scaleBar.setScaleBarMode(DefaultMapScaleBar.ScaleBarMode.SINGLE);
                scaleBar.setDistanceUnitAdapter(ImperialUnitAdapter.INSTANCE);
                break;
            case ScaleBarType.NAUTICAL:
                scaleBar.setScaleBarMode(DefaultMapScaleBar.ScaleBarMode.SINGLE);
                scaleBar.setDistanceUnitAdapter(NauticalUnitAdapter.INSTANCE);
                break;
            case ScaleBarType.METRIC_AND_IMPERIAL:
                scaleBar.setScaleBarMode(DefaultMapScaleBar.ScaleBarMode.BOTH);
                scaleBar.setDistanceUnitAdapter(MetricUnitAdapter.INSTANCE);
                scaleBar.setSecondaryDistanceUnitAdapter(ImperialUnitAdapter.INSTANCE);
                break;
        }

        MapScaleBarLayer mapScaleBarLayer = new MapScaleBarLayer(map, scaleBar);
        BitmapRenderer renderer = mapScaleBarLayer.getRenderer();
        renderer.setPosition(GLViewport.Position.BOTTOM_LEFT);
        renderer.setOffset(5 * CanvasAdapter.getScale(), 0);

        map.layers().add(mapScaleBarLayer);
    }

    private void addLocationLayer(@RawRes int locationFixedMarkerSvgResId, @RawRes int locationNotFixedMarkerSvgResId) {
        InputStream inputStream = null;
        Bitmap fixedBitmap;
        Bitmap notFixedBitmap;

        int width = getResources().getDimensionPixelSize(R.dimen.location_marker_width);
        int height = getResources().getDimensionPixelSize(R.dimen.location_marker_height);

        try {
            inputStream = getResources().openRawResource(locationFixedMarkerSvgResId);
            //TODO: Do this on a background thread
            //TODO: All bitmaps used on the map should be in a TextureAtlas
            fixedBitmap = CanvasAdapter.decodeSvgBitmap(inputStream, width, height, 100);

            inputStream.close();

            inputStream = getResources().openRawResource(locationNotFixedMarkerSvgResId);
            notFixedBitmap = CanvasAdapter.decodeSvgBitmap(inputStream, width, height, 100);
        } catch (IOException e) {
            IOUtils.closeQuietly(inputStream);
            throw new RuntimeException(e.getMessage());
        } finally {
            IOUtils.closeQuietly(inputStream);
        }

        java.util.Map<Object, Bitmap> inputMap = new LinkedHashMap<>();
        java.util.Map<Object, TextureRegion> regionsMap = new LinkedHashMap<>();
        List<TextureAtlas> atlasList = new ArrayList<>();

        inputMap.put(locationFixedMarkerSvgResId, fixedBitmap);
        inputMap.put(locationNotFixedMarkerSvgResId, notFixedBitmap);

        TextureAtlasUtils.createTextureRegions(inputMap, regionsMap, atlasList, true, false);

        locationFixedRegion = regionsMap.get(locationFixedMarkerSvgResId);
        locationNotFixedRegion = regionsMap.get(locationNotFixedMarkerSvgResId);

        locationTextureLayer = new LocationTextureLayer(map, locationNotFixedRegion);
        locationTextureLayer.locationRenderer.setBillboard(false);
        locationTextureLayer.setEnabled(true);

        map.layers().add(locationTextureLayer);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.fragment_map, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        optionsMenu.updateAndroidMenu(menu, getContext());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        MyMenuItem myMenuItem;

        switch (item.getItemId()) {
            case Menu.NONE:
                myMenuItem = optionsMenu.findItem(item, getContext());
                viewModel.onMenuItemSelected(myMenuItem);
                return true;
            default:
                myMenuItem = optionsMenu.findItem(item.getItemId());
                viewModel.onMenuItemSelected(myMenuItem);
        }

        return false;
    }

    @Override
    public void onMapEvent(Event e, MapPosition mapPosition) {
        if (e == Map.MOVE_EVENT || e == Map.SCALE_EVENT || e == Map.ROTATE_EVENT || e == Map.TILT_EVENT) {
            viewModel.onMapPositionChangedByUser(mapPosition);
        }
    }

    @Override
    public void onVisibilityChanged(boolean visible) {
        //TODO:
        /*
         * On at least a Samsung Galaxy S5 mini when moving away from the Mapfragment a black screen is displayed
         * by making the mapView invisible the new fragment is magically displayed as it should be
         */
        mapView.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
    }

    private class GestureLayer extends Layer implements GestureListener {
        GestureLayer(Map map) {
            super(map);
        }

        @Override
        public boolean onGesture(Gesture g, MotionEvent e) {
            if (g == Gesture.LONG_PRESS) {
                viewModel.onMapLongPress();
            }

            return false;
        }
    }
}