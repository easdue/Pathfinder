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

import android.Manifest;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import javax.inject.Inject;
import javax.inject.Singleton;

import nl.erikduisters.pathfinder.data.local.PreferenceManager;
import nl.erikduisters.pathfinder.ui.BaseActivityViewModel;
import nl.erikduisters.pathfinder.ui.fragment.runtime_permission.RuntimePermissionHelper;

/**
 * Created by Erik Duisters on 17-07-2018.
 */

@Singleton
public class GpsStatusActivityViewModel extends BaseActivityViewModel {
    private final MutableLiveData<GpsStatusActivityViewState> viewStateObservable;

    @Inject
    GpsStatusActivityViewModel(PreferenceManager preferenceManager) {
        super(preferenceManager);
        viewStateObservable = new MutableLiveData<>();
    }

    void setRuntimePermissionHelper(RuntimePermissionHelper helper) {
        if (helper.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            viewStateObservable.setValue(new GpsStatusActivityViewState.InitializedState());
        } else {
            viewStateObservable.setValue(new GpsStatusActivityViewState.FinishState());
        }
    }

    LiveData<GpsStatusActivityViewState> getViewStateObservable() { return viewStateObservable; }
}
