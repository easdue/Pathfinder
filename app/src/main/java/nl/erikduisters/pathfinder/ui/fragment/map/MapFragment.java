package nl.erikduisters.pathfinder.ui.fragment.map;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RawRes;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import org.oscim.android.MapView;
import org.oscim.backend.CanvasAdapter;
import org.oscim.backend.canvas.Bitmap;
import org.oscim.core.MapPosition;
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
import org.oscim.renderer.bucket.TextureItem;
import org.oscim.scalebar.DefaultMapScaleBar;
import org.oscim.scalebar.ImperialUnitAdapter;
import org.oscim.scalebar.MapScaleBarLayer;
import org.oscim.scalebar.MetricUnitAdapter;
import org.oscim.scalebar.NauticalUnitAdapter;
import org.oscim.utils.IOUtils;

import java.io.IOException;
import java.io.InputStream;

import butterknife.BindView;
import nl.erikduisters.pathfinder.R;
import nl.erikduisters.pathfinder.data.model.map.LocationLayerInfo;
import nl.erikduisters.pathfinder.data.model.map.ScaleBarType;
import nl.erikduisters.pathfinder.ui.BaseFragment;
import nl.erikduisters.pathfinder.util.menu.MyMenu;
import nl.erikduisters.pathfinder.util.menu.MyMenuItem;

/**
 * Created by Erik Duisters on 28-06-2018.
 */

public class MapFragment extends BaseFragment<MapFragmentViewModel> {
    @BindView(R.id.mapView) MapView mapView;

    private Map map;
    @NonNull
    private MyMenu optionsMenu;
    private MapInitializationState currentMapInitializationState;
    private LocationTextureLayer locationTextureLayer;

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

        viewModel.getMapInitializationStateObservable().observe(this, this::render);
        viewModel.getOptionsMenuObservable().observe(this, this::handleOptionsMenu);
        viewModel.getViewStateObservable().observe(this, this::render);
        viewModel.getMapPositionObservable().observe(this, this::handleMapPosition);
        viewModel.getLocationMarkerInfoObservable().observe(this, this::handleLocationMarkerInfo);

        return v;
    }

    @Override
    public void onDestroyView() {
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
    protected int getLayoutResId() {
        return R.layout.fragment_map;
    }

    @Override
    protected Class<MapFragmentViewModel> getViewModelClass() {
        return MapFragmentViewModel.class;
    }

    private void render(@Nullable MapFragmentViewState viewState) {
        if (viewState == null) {
            return;
        }
    }

    private void render(@Nullable MapInitializationState viewState) {
        if (viewState == null) {
            return;
        }

        if (currentMapInitializationState == null || currentMapInitializationState.tileSource != viewState.tileSource) {
            Layers layers = map.layers();

            for (int i = 2; i < layers.size(); i++) {
                layers.remove(i);
            }

            OsmTileLayer tileLayer = new OsmTileLayer(map);
            tileLayer.setTileSource(viewState.tileSource);

            map.setBaseMap(tileLayer);

            if (viewState.addBuildingLayer) { layers.add(new BuildingLayer(map, tileLayer)); }
            if (viewState.addLabelLayer) { layers.add(new LabelLayer(map, tileLayer)); }

            addScaleBarLayer(viewState.scaleBarType);

            if (viewState.addLocationLayer) { addLocationLayer(viewState.locationMarkerSvgResId); }
        }

        if (currentMapInitializationState == null || currentMapInitializationState.themeFile != viewState.themeFile) {
            map.setTheme(viewState.themeFile);
        }

        currentMapInitializationState = viewState;
    }

    private void addScaleBarLayer(@ScaleBarType int scaleBarType) {
        if (scaleBarType == ScaleBarType.NONE) {
            return;
        }

        DefaultMapScaleBar scaleBar = new DefaultMapScaleBar(map);

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

    private void addLocationLayer(@RawRes int locationMarkerSvgResId) {
        InputStream inputStream = null;
        Bitmap bitmap;

        int width = getResources().getDimensionPixelSize(R.dimen.location_marker_width);
        int height = getResources().getDimensionPixelSize(R.dimen.location_marker_height);

        try {
            inputStream = getResources().openRawResource(locationMarkerSvgResId);
            //TODO: Do this on a background thread
            //TODO: All bitmaps used on the map should be in a TextureAtlas
            bitmap = CanvasAdapter.decodeSvgBitmap(inputStream, width, height, 100);
        } catch (IOException e) {
            IOUtils.closeQuietly(inputStream);
            throw new RuntimeException(e.getMessage());
        }

        TextureAtlas.Rect rect = new TextureAtlas.Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        TextureRegion textureRegion = new TextureRegion(new TextureItem(bitmap), rect);

        locationTextureLayer = new LocationTextureLayer(map, textureRegion);
        locationTextureLayer.locationRenderer.setBillboard(false);
        locationTextureLayer.setEnabled(false);

        map.layers().add(locationTextureLayer);
    }

    private void handleOptionsMenu(@Nullable MyMenu optionsMenu) {
        if (optionsMenu == null) {
            return;
        }

        this.optionsMenu = optionsMenu;
        invalidateOptionsMenu();
    }

    private void handleMapPosition(MapPosition mapPosition) {
        map.setMapPosition(mapPosition);
    }

    private void handleLocationMarkerInfo(LocationLayerInfo info) {
        if (info.hasFix) {
            locationTextureLayer.setEnabled(false);
        } else {
            locationTextureLayer.setEnabled(true);
            locationTextureLayer.setPosition(info.latitude, info.longitude, info.heading, info.accuracy);
        }
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
        switch(item.getItemId()) {
            case Menu.NONE:
                MyMenuItem myMenuItem = optionsMenu.findItem(item, getContext());
                viewModel.onMenuItemSelected(myMenuItem);
                return true;
        }

        return false;
    }
}
