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

package nl.erikduisters.pathfinder.ui.fragment.gps_status;

import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import nl.erikduisters.pathfinder.R;
import nl.erikduisters.pathfinder.data.local.GpsManager;
import nl.erikduisters.pathfinder.ui.BaseFragment;
import nl.erikduisters.pathfinder.ui.fragment.gps_status.GpsStatusFragmentViewState.DataState;
import nl.erikduisters.pathfinder.ui.fragment.gps_status.GpsStatusFragmentViewState.GpsNotEnabledState;
import nl.erikduisters.pathfinder.ui.fragment.gps_status.GpsStatusFragmentViewState.ShowEnableGpsSettingState;
import nl.erikduisters.pathfinder.ui.fragment.gps_status.GpsStatusFragmentViewState.WaitingForGpsToBeEnabledState;
import nl.erikduisters.pathfinder.ui.widget.GpsSkyView;
import nl.erikduisters.pathfinder.ui.widget.VerticalProgressBar;

/**
 * Created by Erik Duisters on 17-07-2018.
 */
public class GpsStatusFragment extends BaseFragment<GpsStatusFragmentViewModel> {
    @BindView(R.id.gpsstatus_time) TextView time;
    @BindView(R.id.gpsstatus_latitude) TextView latitude;
    @BindView(R.id.gpsstatus_longitude) TextView longitude;
    @BindView(R.id.gpsstatus_accuracy) TextView accuracy;
    @BindView(R.id.gpsstatus_altitude) TextView altitude;
    @BindView(R.id.gpsstatus_heading) TextView heading;
    @BindView(R.id.gpsstatus_speed) TextView speed;
    @BindView(R.id.gpsSkyView) GpsSkyView gpsSkyView;
    @BindView(R.id.gpsstatus_layout_gps_enabled) View gpsEnabledView;
    @BindView(R.id.gpsstatus_layout_gps_disabled) View gpsDisabledView;
    @BindView(R.id.gpsstatus_button_enable_gps) Button buttonEnableGps;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel.getViewStateObservable().observe(this, this::render);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);

        satBars = new ArrayList<>();

        VerticalProgressBar verticalProgressBar;

        for (int i = 0; ; i++) {
            verticalProgressBar = (VerticalProgressBar) v.findViewWithTag("satStatus_Bar" + (i + 1));
            if (verticalProgressBar == null) {
                break;
            }

            verticalProgressBar.setMax(50);
            satBars.add(verticalProgressBar);
        }

        satBarBounds = satBars.get(0).getProgressDrawable().getBounds();

        noFixConstantState = ContextCompat.getDrawable(getContext(), R.drawable.vertical_progress_bar_nofix).getConstantState();
        fixConstantState = ContextCompat.getDrawable(getContext(), R.drawable.vertical_progress_bar_fix).getConstantState();

        buttonEnableGps.setOnClickListener(v1 -> viewModel.onUserWantsToEnableGps());

        return v;
    }

    private ArrayList<VerticalProgressBar> satBars = null;
    private Rect satBarBounds;
    private Drawable.ConstantState noFixConstantState;
    private Drawable.ConstantState fixConstantState;

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_gps_status;
    }

    @Override
    protected Class<GpsStatusFragmentViewModel> getViewModelClass() {
        return GpsStatusFragmentViewModel.class;
    }

    @Override
    public void onResume() {
        super.onResume();

        viewModel.start();
    }

    public void onFinish() {
        viewModel.finish();
    }

    private void render(@Nullable GpsStatusFragmentViewState viewState) {
        if (viewState == null) {
            return;
        }

        if (viewState instanceof GpsNotEnabledState) {
            clearData();
            gpsDisabledView.setVisibility(View.VISIBLE);
            gpsEnabledView.setVisibility(View.GONE);
        }

        if (viewState instanceof ShowEnableGpsSettingState) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);

            viewModel.onEnableGpsSettingsShown();
        }

        if (viewState instanceof WaitingForGpsToBeEnabledState) {
            //Do nothing
        }

        if (viewState instanceof DataState) {
            gpsDisabledView.setVisibility(View.GONE);
            gpsEnabledView.setVisibility(View.VISIBLE);

            render((DataState) viewState);
        }
    }

    private void render(DataState state) {
        updateSatBars(state.satInfoList);
        gpsSkyView.setSatellites(state.satInfoList);
        updateLocationInfo(state);
    }

    private void updateSatBars(List<GpsManager.SatelliteInfo> satInfoList) {
        int i = 0;
        Drawable progress;
        VerticalProgressBar progressBar;

        for (GpsManager.SatelliteInfo sat : satInfoList) {
            if (i >= satBars.size()) {
                break;
            }

            if (sat.usedInFix) {
                progress = fixConstantState.newDrawable();
            } else {
                progress = noFixConstantState.newDrawable();
            }

            progress.setBounds(satBarBounds);

            progressBar = satBars.get(i);
            progressBar.setProgressDrawable(progress);
            progressBar.setProgress((int) sat.snr);
            progressBar.setText(String.valueOf(sat.prn));
            i++;
        }
    }

    private void updateLocationInfo(DataState state) {
        time.setText(state.time);
        latitude.setText(state.coordinate.getLatitudeAsString(getContext()));
        longitude.setText(state.coordinate.getLongitudeAsString(getContext()));
        accuracy.setText(state.accuracy.getDistance(getContext()));
        altitude.setText(state.altitude.getDistance(getContext()));
        heading.setText(state.heading.asString());
        speed.setText(state.speed.getSpeed(getContext()));
    }

    private void clearData() {
        time.setText("");
        latitude.setText("");
        longitude.setText("");
        accuracy.setText("");
        altitude.setText("");
        heading.setText("");
        speed.setText("");

        for (VerticalProgressBar bar : satBars) {
            bar.setProgress(0);
        }

        gpsSkyView.setSatellites(null);
    }
}
