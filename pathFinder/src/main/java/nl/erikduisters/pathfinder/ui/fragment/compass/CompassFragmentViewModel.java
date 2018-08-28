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

package nl.erikduisters.pathfinder.ui.fragment.compass;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.location.Location;
import android.support.annotation.NonNull;

import javax.inject.Inject;
import javax.inject.Singleton;

import nl.erikduisters.pathfinder.data.local.GpsManager;
import nl.erikduisters.pathfinder.data.local.HeadingManager;
import nl.erikduisters.pathfinder.data.local.PreferenceManager;
import nl.erikduisters.pathfinder.util.Distance;
import nl.erikduisters.pathfinder.util.IntegerDegrees;
import nl.erikduisters.pathfinder.util.Speed;
import nl.erikduisters.pathfinder.util.UnitsUtil;
import nl.erikduisters.pathfinder.util.menu.MyMenu;
import timber.log.Timber;

/**
 * Created by Erik Duisters on 28-06-2018.
 */

//TODO: TimeToNext, ETA
//TODO: Keep screen on
@Singleton
public class CompassFragmentViewModel extends ViewModel implements GpsManager.LocationListener, HeadingManager.HeadingListener {
    private final MutableLiveData<CompassFragmentViewState> viewStateObservable;

    private final PreferenceManager preferenceManager;
    private final GpsManager gpsManager;
    private final HeadingManager headingManager;

    private CompassFragmentViewState.Builder builder;

    @Inject
    CompassFragmentViewModel(PreferenceManager preferenceManager, GpsManager gpsManager, HeadingManager headingManager) {
        this.preferenceManager = preferenceManager;
        this.gpsManager = gpsManager;
        this.headingManager = headingManager;

        viewStateObservable = new MutableLiveData<>();

        initBuilder();

        viewStateObservable.setValue(builder.build());
    }

    public LiveData<CompassFragmentViewState> getViewStateObservable() { return viewStateObservable; }

    private void initBuilder() {
        builder = new CompassFragmentViewState.Builder()
                .withOptionsMenu(createOptionsMenu())
                .withBearing(new IntegerDegrees())
                .withHeading(new IntegerDegrees())
                .withDistanceToNext(new Distance(Distance.UNKNOWN_DISTANCE, 0))
                .withSpeed(new Speed(0, 0));
    }

    private MyMenu createOptionsMenu() {
        //TODO: Add menu items
        return new MyMenu();
    }

    void start() {
        Timber.d("Start listening to sensors");
        gpsManager.addLocationListener(this);

        headingManager.addHeadingListener(this);
    }

    void stop() {
        Timber.d("stop listening to sensors");
        gpsManager.removeLocationListener(this);

        headingManager.removeHeadingListener(this);
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        double speed = UnitsUtil.metersPerSecond2KilometersPerHour(location.hasSpeed() ? location.getSpeed() : 0);

        builder.withSpeed(new Speed(speed, 2));

        viewStateObservable.setValue(builder.build());
    }

    @Override
    public void onHeadingChanged(IntegerDegrees heading) {
        Timber.e("onHeadingChanged");
        builder.withHeading(heading);

        viewStateObservable.setValue(builder.build());
    }
}
