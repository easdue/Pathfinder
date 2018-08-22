package nl.erikduisters.pathfinder.ui.activity.map_download;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import butterknife.BindView;
import nl.erikduisters.pathfinder.R;
import nl.erikduisters.pathfinder.service.MapDownloadService;
import nl.erikduisters.pathfinder.ui.BaseActivity;
import nl.erikduisters.pathfinder.ui.fragment.map_download.MapDownloadFragment;
import nl.erikduisters.pathfinder.viewmodel.VoidViewModel;

/**
 * Created by Erik Duisters on 26-07-2018.
 */
public class MapDownloadActivity extends BaseActivity<VoidViewModel> {
    private static final String TAG_MAP_DOWNLOAD_FRAGMENT = "MapDownloadFragment";

    @BindView(R.id.contraintLayout) ConstraintLayout constraintLayout;
    @BindView(R.id.toolbar) Toolbar toolbar;
    private MapDownloadFragment mapDownloadFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mapDownloadFragment = findFragment(TAG_MAP_DOWNLOAD_FRAGMENT);

        if (mapDownloadFragment == null) {
            mapDownloadFragment = new MapDownloadFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragmentPlaceHolder, mapDownloadFragment, TAG_MAP_DOWNLOAD_FRAGMENT)
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!mapDownloadFragment.onBackPressed()) {
            super.onBackPressed();
        }
    }

    @Override
    protected void bindMapDownloadService() {
        Intent intent = new Intent(this, MapDownloadService.class);
        bindService(intent, mapDownloadServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_map_download;
    }

    @Override
    protected Class<VoidViewModel> getViewModelClass() {
        return VoidViewModel.class;
    }

    @Override
    protected View getCoordinatorLayoutOrRootView() {
        return constraintLayout;
    }

    @Override
    public void onMapDownloadComplete() {
        //Don't unbind because downloads can be started
    }
}
