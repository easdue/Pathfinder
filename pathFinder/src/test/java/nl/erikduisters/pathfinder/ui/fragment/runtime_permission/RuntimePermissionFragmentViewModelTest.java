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

import android.Manifest;
import android.arch.core.executor.testing.InstantTaskExecutorRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import nl.erikduisters.pathfinder.R;
import nl.erikduisters.pathfinder.ui.dialog.MessageWithTitle;
import nl.erikduisters.pathfinder.ui.fragment.runtime_permission.RuntimePermissionFragmentViewState.RequestRuntimePermissionState;
import nl.erikduisters.pathfinder.ui.fragment.runtime_permission.RuntimePermissionFragmentViewState.RequestingRuntimePermissionState;
import nl.erikduisters.pathfinder.ui.fragment.runtime_permission.RuntimePermissionFragmentViewState.RuntimePermissionResultState;
import nl.erikduisters.pathfinder.ui.fragment.runtime_permission.RuntimePermissionFragmentViewState.ShowPermissionRationaleState;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Created by Erik Duisters on 13-06-2018.
 */
@RunWith(JUnit4.class)
public class RuntimePermissionFragmentViewModelTest {
    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    RuntimePermissionHelper runtimePermissionHelper;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void noRuntimePermissionHelperSetWhenRequestingPermission_resultsInRuntimeException() {
        thrown.expect(RuntimeException.class);
        thrown.expectMessage("You must call setRuntimePermissionHelper before calling requestPermission");

        RuntimePermissionFragmentViewModel viewModel = new RuntimePermissionFragmentViewModel();
        MessageWithTitle rationale = new MessageWithTitle(R.string.permission_needed, R.string.location_permission_rational);
        RuntimePermissionRequest request = new RuntimePermissionRequest(Manifest.permission.ACCESS_FINE_LOCATION, rationale);
        viewModel.requestPermission(request);
    }

    @Test
    public void requestingAnAlreadyGrantedPermission_resultsInRuntimePermissionResultState() {
        when(runtimePermissionHelper.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)).thenReturn(true);
        RuntimePermissionFragmentViewModel viewModel = new RuntimePermissionFragmentViewModel();
        viewModel.setRuntimePermissionHelper(runtimePermissionHelper);

        MessageWithTitle rationale = new MessageWithTitle(R.string.permission_needed, R.string.location_permission_rational);
        RuntimePermissionRequest request = new RuntimePermissionRequest(Manifest.permission.ACCESS_FINE_LOCATION, rationale);
        viewModel.requestPermission(request);

        assert(viewModel.getViewStateObservable().getValue() instanceof RuntimePermissionResultState);
        RuntimePermissionResultState viewState = (RuntimePermissionResultState) viewModel.getViewStateObservable().getValue();
        assert(viewState.permission.equals(request.getPermission()));
        assertTrue(viewState.isGranted);
    }

    @Test
    public void requestingAnNeverBeforeRequestedPermission_resultsInRequestPermissionState() {
        when(runtimePermissionHelper.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)).thenReturn(false);
        when(runtimePermissionHelper.shouldShowPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)).thenReturn(false);

        RuntimePermissionFragmentViewModel viewModel = new RuntimePermissionFragmentViewModel();
        viewModel.setRuntimePermissionHelper(runtimePermissionHelper);

        MessageWithTitle rationale = new MessageWithTitle(R.string.permission_needed, R.string.location_permission_rational);
        RuntimePermissionRequest request = new RuntimePermissionRequest(Manifest.permission.ACCESS_FINE_LOCATION, rationale);
        viewModel.requestPermission(request);

        assert(viewModel.getViewStateObservable().getValue() instanceof RequestRuntimePermissionState);
        RequestRuntimePermissionState viewState = (RequestRuntimePermissionState) viewModel.getViewStateObservable().getValue();
        assert(viewState.runtimePermissionRequest == request);
    }

    @Test
    public void requestingAPreviouslyDeniedPermission_resultsInShowPermissionRationaleState() {
        when(runtimePermissionHelper.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)).thenReturn(false);
        when(runtimePermissionHelper.shouldShowPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)).thenReturn(true);

        RuntimePermissionFragmentViewModel viewModel = new RuntimePermissionFragmentViewModel();
        viewModel.setRuntimePermissionHelper(runtimePermissionHelper);

        MessageWithTitle rationale = new MessageWithTitle(R.string.permission_needed, R.string.location_permission_rational);
        RuntimePermissionRequest request = new RuntimePermissionRequest(Manifest.permission.ACCESS_FINE_LOCATION, rationale);
        viewModel.requestPermission(request);

        assert(viewModel.getViewStateObservable().getValue() instanceof ShowPermissionRationaleState);
        ShowPermissionRationaleState viewState = (ShowPermissionRationaleState) viewModel.getViewStateObservable().getValue();
        assert(viewState.runtimePermissionRequest == request);
    }

    @Test
    public void callingOnPermissionRequested_resultsInRequestingRuntimePermissionState() {
        when(runtimePermissionHelper.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)).thenReturn(false);
        when(runtimePermissionHelper.shouldShowPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)).thenReturn(false);

        RuntimePermissionFragmentViewModel viewModel = new RuntimePermissionFragmentViewModel();
        viewModel.setRuntimePermissionHelper(runtimePermissionHelper);

        MessageWithTitle rationale = new MessageWithTitle(R.string.permission_needed, R.string.location_permission_rational);
        RuntimePermissionRequest request = new RuntimePermissionRequest(Manifest.permission.ACCESS_FINE_LOCATION, rationale);
        viewModel.onPermissionRequested(request);

        assert(viewModel.getViewStateObservable().getValue() instanceof RequestingRuntimePermissionState);
        RequestingRuntimePermissionState viewState = (RequestingRuntimePermissionState) viewModel.getViewStateObservable().getValue();
        assert(viewState.runtimePermissionRequest == request);
    }

    @Test
    public void callingOnPermissionRationaleAccepted_resultsInRequestRuntimePermissionState() {
        when(runtimePermissionHelper.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)).thenReturn(false);
        when(runtimePermissionHelper.shouldShowPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)).thenReturn(false);

        RuntimePermissionFragmentViewModel viewModel = new RuntimePermissionFragmentViewModel();
        viewModel.setRuntimePermissionHelper(runtimePermissionHelper);

        MessageWithTitle rationale = new MessageWithTitle(R.string.permission_needed, R.string.location_permission_rational);
        RuntimePermissionRequest request = new RuntimePermissionRequest(Manifest.permission.ACCESS_FINE_LOCATION, rationale);
        viewModel.onPermissionRationaleAccepted(request);

        assert(viewModel.getViewStateObservable().getValue() instanceof RequestRuntimePermissionState);
        RequestRuntimePermissionState viewState = (RequestRuntimePermissionState) viewModel.getViewStateObservable().getValue();
        assert(viewState.runtimePermissionRequest == request);
    }

    @Test
    public void callingOnPermissioNRationaleDenied_resultsInRuntimePermissionResultState() {
        when(runtimePermissionHelper.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)).thenReturn(false);
        when(runtimePermissionHelper.shouldShowPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)).thenReturn(false);

        RuntimePermissionFragmentViewModel viewModel = new RuntimePermissionFragmentViewModel();
        viewModel.setRuntimePermissionHelper(runtimePermissionHelper);

        MessageWithTitle rationale = new MessageWithTitle(R.string.permission_needed, R.string.location_permission_rational);
        RuntimePermissionRequest request = new RuntimePermissionRequest(Manifest.permission.ACCESS_FINE_LOCATION, rationale);
        viewModel.onPermissionRationaleDenied(request);

        assert(viewModel.getViewStateObservable().getValue() instanceof RuntimePermissionResultState);
        RuntimePermissionResultState viewState = (RuntimePermissionResultState) viewModel.getViewStateObservable().getValue();
        assert(viewState.permission.equals(request.getPermission()));
        assertFalse(viewState.isGranted);
    }

    @Test
    public void callingDeniedOnPermissionRequestResultWith_resultsInRuntimePermissionResultState() {
        RuntimePermissionFragmentViewModel viewModel = new RuntimePermissionFragmentViewModel();
        viewModel.onPermissionRequestResult(Manifest.permission.ACCESS_FINE_LOCATION, false);

        assert(viewModel.getViewStateObservable().getValue() instanceof RuntimePermissionResultState);
        RuntimePermissionResultState viewState = (RuntimePermissionResultState) viewModel.getViewStateObservable().getValue();
        assert(viewState.permission.equals(Manifest.permission.ACCESS_FINE_LOCATION));
        assertFalse(viewState.isGranted);
    }

    @Test
    public void callingGrantedOnPermissionRequestResultWith_resultsInRuntimePermissionResultState() {
        RuntimePermissionFragmentViewModel viewModel = new RuntimePermissionFragmentViewModel();
        viewModel.onPermissionRequestResult(Manifest.permission.ACCESS_FINE_LOCATION, true);

        assert(viewModel.getViewStateObservable().getValue() instanceof RuntimePermissionResultState);
        RuntimePermissionResultState viewState = (RuntimePermissionResultState) viewModel.getViewStateObservable().getValue();
        assert(viewState.permission.equals(Manifest.permission.ACCESS_FINE_LOCATION));
        assertTrue(viewState.isGranted);
    }

    @Test
    public void callingOnRuntimePermissionResultStateReported_resultsInNullState() {
        RuntimePermissionFragmentViewModel viewModel = new RuntimePermissionFragmentViewModel();
        viewModel.onRuntimePermissionResultStateReported();

        assert(viewModel.getViewStateObservable().getValue() == null);
    }

    @Test
    public void multipleRuntimePermissionRequestsAreHandledSequentially() {
        when(runtimePermissionHelper.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)).thenReturn(true);
        when(runtimePermissionHelper.hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)).thenReturn(true);
        when(runtimePermissionHelper.shouldShowPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)).thenReturn(false);

        RuntimePermissionFragmentViewModel viewModel = new RuntimePermissionFragmentViewModel();
        viewModel.setRuntimePermissionHelper(runtimePermissionHelper);

        MessageWithTitle rationale = new MessageWithTitle(R.string.permission_needed, R.string.location_permission_rational);
        RuntimePermissionRequest request = new RuntimePermissionRequest(Manifest.permission.ACCESS_FINE_LOCATION, rationale);

        viewModel.requestPermission(request);

        request = new RuntimePermissionRequest(Manifest.permission.ACCESS_COARSE_LOCATION, rationale);

        viewModel.requestPermission(request);

        assert(viewModel.getViewStateObservable().getValue() instanceof RuntimePermissionResultState);
        RuntimePermissionResultState state = (RuntimePermissionResultState) viewModel.getViewStateObservable().getValue();
        assert(state.permission.equals(Manifest.permission.ACCESS_FINE_LOCATION));
        assertTrue(state.isGranted);

        viewModel.onRuntimePermissionResultStateReported();

        assert(viewModel.getViewStateObservable().getValue() instanceof RuntimePermissionResultState);
        state = (RuntimePermissionResultState) viewModel.getViewStateObservable().getValue();
        assert(state.permission.equals(Manifest.permission.ACCESS_COARSE_LOCATION));
        assertTrue(state.isGranted);
    }
}