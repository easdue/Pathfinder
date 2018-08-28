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

package nl.erikduisters.pathfinder.ui.activity.gps_status;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import butterknife.BindView;
import nl.erikduisters.pathfinder.R;
import nl.erikduisters.pathfinder.ui.BaseActivity;
import nl.erikduisters.pathfinder.ui.fragment.gps_status.GpsStatusFragment;
import nl.erikduisters.pathfinder.ui.fragment.runtime_permission.RuntimePermissionHelper;

/**
 * Created by Erik Duisters on 17-07-2018.
 */
public class GpsStatusActivity
        extends BaseActivity<GpsStatusActivityViewModel>
        implements RuntimePermissionHelper {

    @BindView(R.id.contraintLayout) ConstraintLayout constraintLayout;
    @BindView(R.id.toolbar) Toolbar toolbar;
    private GpsStatusFragment gpsStatusFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        gpsStatusFragment = findFragment(R.id.gpsStatusFragment);

        viewModel.setRuntimePermissionHelper(this);

        viewModel.getViewStateObservable().observe(this, this::render);
    }

    private void render(@Nullable GpsStatusActivityViewState viewState) {
        if (viewState == null) {
            return;
        }

        if (viewState instanceof GpsStatusActivityViewState.FinishState) {
            finish();
        }

        if (viewState instanceof GpsStatusActivityViewState.InitializedState) {
            //Do nothing
        }
    }

    @Override
    public void onBackPressed() {
        gpsStatusFragment.onFinish();
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                gpsStatusFragment.onFinish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_gps_status;
    }

    @Override
    protected Class<GpsStatusActivityViewModel> getViewModelClass() {
        return GpsStatusActivityViewModel.class;
    }

    @Override
    protected View getCoordinatorLayoutOrRootView() {
        return constraintLayout;
    }

    @Override
    public boolean hasPermission(String permission) {
        //noinspection ConstantConditions
        return ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public boolean shouldShowPermissionRationale(String permission) {
        return false;
    }
}
