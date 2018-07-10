package nl.erikduisters.pathfinder.ui.fragment.map;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import org.oscim.android.MapView;
import org.oscim.layers.tile.buildings.BuildingLayer;
import org.oscim.layers.tile.vector.OsmTileLayer;
import org.oscim.layers.tile.vector.labeling.LabelLayer;
import org.oscim.map.Layers;
import org.oscim.map.Map;

import butterknife.BindView;
import nl.erikduisters.pathfinder.R;
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
        }

        if (currentMapInitializationState == null || currentMapInitializationState.themeFile != viewState.themeFile) {
            map.setTheme(viewState.themeFile);
        }

        if (currentMapInitializationState == null || currentMapInitializationState.mapPosition != viewState.mapPosition) {
            map.setMapPosition(viewState.mapPosition);
        }

        currentMapInitializationState = viewState;
    }

    private void handleOptionsMenu(@Nullable MyMenu optionsMenu) {
        if (optionsMenu == null) {
            return;
        }

        this.optionsMenu = optionsMenu;
        invalidateOptionsMenu();
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
