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

package nl.erikduisters.pathfinder.ui.fragment.runtime_permission;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayDeque;

import javax.inject.Inject;
import javax.inject.Singleton;

import nl.erikduisters.pathfinder.ui.fragment.runtime_permission.RuntimePermissionFragmentViewState.RequestRuntimePermissionState;
import nl.erikduisters.pathfinder.ui.fragment.runtime_permission.RuntimePermissionFragmentViewState.RequestingRuntimePermissionState;
import nl.erikduisters.pathfinder.ui.fragment.runtime_permission.RuntimePermissionFragmentViewState.RuntimePermissionResultState;
import nl.erikduisters.pathfinder.ui.fragment.runtime_permission.RuntimePermissionFragmentViewState.ShowPermissionRationaleState;
import timber.log.Timber;

@Singleton
public class RuntimePermissionFragmentViewModel extends ViewModel {
    private @Nullable RuntimePermissionHelper runtimePermissionHelper;
    private MutableLiveData<RuntimePermissionFragmentViewState> viewStateObservable;
    private ArrayDeque<RuntimePermissionRequest> runtimePermissionsToRequest;
    private RuntimePermissionRequest currentRuntimePermissionRequest;

    @Inject
    RuntimePermissionFragmentViewModel() {
        Timber.d("new RuntimePermissionFragmentViewModel created");
        viewStateObservable = new MutableLiveData<>();
        runtimePermissionsToRequest = new ArrayDeque<>();
    }

    LiveData<RuntimePermissionFragmentViewState> getViewStateObservable() { return viewStateObservable; }

    void setRuntimePermissionHelper(@Nullable RuntimePermissionHelper runtimePermissionHelper) {
        this.runtimePermissionHelper = runtimePermissionHelper;
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        //Don't leak RuntimePermissionFragment
        runtimePermissionHelper = null;
    }

    public void requestPermission(@NonNull RuntimePermissionRequest runtimePermissionRequest) {
        if (runtimePermissionHelper == null) {
            throw new RuntimeException("You must call setRuntimePermissionHelper before calling requestPermission");
        }

        if (runtimePermissionsToRequest.contains(runtimePermissionRequest) ||
                (currentRuntimePermissionRequest != null && currentRuntimePermissionRequest.equals(runtimePermissionRequest))) {
            return;
        }

        runtimePermissionsToRequest.push(runtimePermissionRequest);

        if (viewStateObservable.getValue() == null) {
            processNextPermissionRequest();
        }
    }

    private void processNextPermissionRequest() {
        if (runtimePermissionHelper == null || runtimePermissionsToRequest.isEmpty()) {
            return;
        }

        currentRuntimePermissionRequest = runtimePermissionsToRequest.poll();

        if (runtimePermissionHelper.hasPermission(currentRuntimePermissionRequest.getPermission())) {
            Timber.d("Permission: %s is already granted, calling onPermissionGranted()", currentRuntimePermissionRequest.getPermission());
            onPermissionRequestResult(currentRuntimePermissionRequest.getPermission(), true);
        } else if (runtimePermissionHelper.shouldShowPermissionRationale(currentRuntimePermissionRequest.getPermission())) {
            Timber.d("Calling showPermissionRationale()");
            ShowPermissionRationaleState state = new ShowPermissionRationaleState(currentRuntimePermissionRequest);
            viewStateObservable.setValue(state);
        } else {
            RequestRuntimePermissionState state = new RequestRuntimePermissionState(currentRuntimePermissionRequest);
            viewStateObservable.setValue(state);
        }
    }

    public void onPermissionRequested(@NonNull RuntimePermissionRequest request) {
        RequestingRuntimePermissionState state = new RequestingRuntimePermissionState(request);
        viewStateObservable.setValue(state);
    }

    public void onPermissionRationaleAccepted(@NonNull RuntimePermissionRequest request) {
        RequestRuntimePermissionState state = new RequestRuntimePermissionState(request);
        viewStateObservable.setValue(state);

    }

    public void onPermissionRationaleDenied(@NonNull RuntimePermissionRequest request) {
        Timber.d("onPermissionRationaleDenied, calling onPermissionDenied()");
        onPermissionRequestResult(request.getPermission(), false);
    }

    public void onPermissionRequestResult(@NonNull String permission, boolean isGranted) {
        currentRuntimePermissionRequest = null;

        RuntimePermissionResultState state = new RuntimePermissionResultState(permission, isGranted);
        viewStateObservable.setValue(state);
    }

    public void onRuntimePermissionResultStateReported() {
        viewStateObservable.setValue(null);

        processNextPermissionRequest();
    }
}
