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

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.location.Location;
import android.support.annotation.NonNull;

import java.util.ArrayList;

import javax.inject.Inject;
import javax.inject.Singleton;

import nl.erikduisters.pathfinder.data.local.GpsManager;
import nl.erikduisters.pathfinder.data.local.PreferenceManager;
import nl.erikduisters.pathfinder.ui.fragment.gps_status.GpsStatusFragmentViewState.DataState;
import nl.erikduisters.pathfinder.util.Coordinate;
import nl.erikduisters.pathfinder.util.DateUtil;
import nl.erikduisters.pathfinder.util.Distance;
import nl.erikduisters.pathfinder.util.IntegerDegrees;
import nl.erikduisters.pathfinder.util.Speed;

/**
 * Created by Erik Duisters on 17-07-2018.
 */

@Singleton
public class GpsStatusFragmentViewModel extends ViewModel implements GpsManager.LocationListener, GpsManager.GpsStatusChangedListener {
    private final MutableLiveData<GpsStatusFragmentViewState> viewStateObservable;
    private final GpsManager gpsManager;
    private final PreferenceManager preferenceManager;
    private final DataState.Builder dataStateBuilder;

    @Inject
    GpsStatusFragmentViewModel(GpsManager gpsManager, PreferenceManager preferenceManager) {
        viewStateObservable = new MutableLiveData<>();
        this.gpsManager = gpsManager;
        this.preferenceManager = preferenceManager;
        dataStateBuilder = new DataState.Builder();
    }

    LiveData<GpsStatusFragmentViewState> getViewStateObservable() { return viewStateObservable; }

    void start() {
        if (gpsManager.isGpsEnabled()) {
            if (!(viewStateObservable.getValue() instanceof DataState)) {
                setInitialDataState();

                gpsManager.addLocationListener(this);
                gpsManager.setGpsStatusChangedListener(this);
            }
        } else {
            viewStateObservable.setValue(new GpsStatusFragmentViewState.GpsNotEnabledState());
        }
    }

    void finish() {
        gpsManager.removeLocationListener(this);
        gpsManager.setGpsStatusChangedListener(null);
        viewStateObservable.setValue(null);
    }

    private void setInitialDataState() {
        dataStateBuilder
                .withSatInfoList(new ArrayList<>())
                .withTime("")
                .withCoordinate(new Coordinate())
                .withAccuracy(new Distance(Distance.UNKNOWN_DISTANCE, 1))
                .withAltitude(new Distance(Distance.UNKNOWN_DISTANCE, 1))
                .withHeading(new IntegerDegrees())
                .withSpeed(new Speed(Speed.UNKNOWN_SPEED, 0));

        viewStateObservable.setValue(dataStateBuilder.build());
    }

    void onUserWantsToEnableGps() {
        viewStateObservable.setValue(new GpsStatusFragmentViewState.ShowEnableGpsSettingState());
    }

    void onEnableGpsSettingsShown() {
        viewStateObservable.setValue(new GpsStatusFragmentViewState.WaitingForGpsToBeEnabledState());
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        dataStateBuilder
                .withTime(DateUtil.utcTime(location.getTime()))
                .withCoordinate(new Coordinate(location.getLatitude(), location.getLongitude()))
                .withAccuracy(new Distance(location.getAccuracy(), 1))
                .withAltitude(new Distance(location.hasAltitude() ? location.getAltitude() : Distance.UNKNOWN_DISTANCE, 1))
                .withHeading(new IntegerDegrees(location.hasBearing() ? (int) location.getBearing() : IntegerDegrees.UNKNOWN))
                .withSpeed(new Speed(location.hasSpeed() ? location.getSpeed() : Speed.UNKNOWN_SPEED, 0));

        viewStateObservable.setValue(dataStateBuilder.build());
    }

    @Override
    public void onSatelliteStatusChanged(@NonNull ArrayList<GpsManager.SatelliteInfo> satInfoList) {
        dataStateBuilder.withSatInfoList(satInfoList);

        viewStateObservable.setValue(dataStateBuilder.build());
    }

    @Override
    public void onGpsStarted() {

    }

    @Override
    public void onGpsStopped() {

    }
}
