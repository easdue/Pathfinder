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

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

import nl.erikduisters.pathfinder.ui.BaseFragment;
import nl.erikduisters.pathfinder.ui.RequestCode;
import nl.erikduisters.pathfinder.ui.dialog.PermissionRationaleDialog;
import nl.erikduisters.pathfinder.ui.fragment.HeadlessFragment;
import nl.erikduisters.pathfinder.ui.fragment.runtime_permission.RuntimePermissionFragmentViewState.RequestRuntimePermissionState;
import nl.erikduisters.pathfinder.ui.fragment.runtime_permission.RuntimePermissionFragmentViewState.RequestingRuntimePermissionState;
import nl.erikduisters.pathfinder.ui.fragment.runtime_permission.RuntimePermissionFragmentViewState.RuntimePermissionResultState;
import nl.erikduisters.pathfinder.ui.fragment.runtime_permission.RuntimePermissionFragmentViewState.ShowPermissionRationaleState;
import timber.log.Timber;

/**
 * Created by Erik Duisters on 09-06-2018.
 */

public class RuntimePermissionFragment
        extends BaseFragment<RuntimePermissionFragmentViewModel>
        implements RuntimePermissionHelper, PermissionRationaleDialog.Listener, HeadlessFragment {

    public interface RuntimePermissionFragmentListener {
        void onPermissionGranted(@NonNull String permission);
        void onPermissionDenied(@NonNull String permission);
    }

    private static final String TAG_PERMISSION_RATIONALE_DIALOG = "PermissionRationaleDialog";

    private static final String KEY_PERMISSION_TO_REQUEST = "PermissionToRequest";

    private RuntimePermissionFragmentListener listener;

    public RuntimePermissionFragment() {}

    public void setListener(RuntimePermissionFragmentListener listener) {
        this.listener = listener;
    }

    public static RuntimePermissionFragment newInstance(@NonNull RuntimePermissionRequest runtimePermissionRequest) {
        Bundle args = new Bundle();
        args.putParcelable(KEY_PERMISSION_TO_REQUEST, runtimePermissionRequest);

        RuntimePermissionFragment fragment = new RuntimePermissionFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return 0;
    }

    @Override
    protected Class<RuntimePermissionFragmentViewModel> getViewModelClass() {
        return RuntimePermissionFragmentViewModel.class;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel.setRuntimePermissionHelper(this);

        Bundle args = getArguments();

        if (args != null && args.containsKey(KEY_PERMISSION_TO_REQUEST)) {
            RuntimePermissionRequest request = args.getParcelable(KEY_PERMISSION_TO_REQUEST);
            viewModel.requestPermission(request);
            args.remove(KEY_PERMISSION_TO_REQUEST);
        }

        viewModel.getViewStateObservable().observe(this, this::render);
    }

    @Override
    public void onDestroy() {
        viewModel.setRuntimePermissionHelper(null);
        super.onDestroy();
    }

    @Nullable
    public RuntimePermissionFragmentViewModel getViewModel() { return viewModel; }

    @Override
    public boolean hasPermission(String permission) {
        //noinspection ConstantConditions
        return ActivityCompat.checkSelfPermission(getContext(), permission) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public boolean shouldShowPermissionRationale(String permission) {
        return shouldShowRequestPermissionRationale(permission);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Timber.d("onRequestPermissionsResult()");

        if (permissions.length == 0) {
            Timber.d("No permissions granted or denied");
            return;
        }

        RequestingRuntimePermissionState state = (RequestingRuntimePermissionState) viewModel.getViewStateObservable().getValue();

        if (state == null) {
            throw new RuntimeException("Received a runtime permission result but there is no current RequestingRuntimePermissionState");
        }

        String permission = permissions[0];
        boolean permissionGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

        if (!permission.equals(state.runtimePermissionRequest.getPermission())) {
            throw new RuntimeException("Received a permission result for a permission i did not runtimePermissionRequest");
        }

        viewModel.onPermissionRequestResult(permission, permissionGranted);
    }

    public void render(@Nullable RuntimePermissionFragmentViewState state) {
        Timber.d("render(viewState == %s)", state == null ? "null" : state.getClass().getSimpleName());

        if (state == null) return;

        if (state instanceof RequestRuntimePermissionState) {
            requestPermission((RequestRuntimePermissionState) state);
        }

        if (state instanceof RequestingRuntimePermissionState) {
            return;
        }

        if (state instanceof ShowPermissionRationaleState) {
            showRunTimePermissionRationale(TAG_PERMISSION_RATIONALE_DIALOG, ((ShowPermissionRationaleState)state).runtimePermissionRequest);
        } else {
            dismissDialogFragment(TAG_PERMISSION_RATIONALE_DIALOG);
        }

        if (state instanceof RuntimePermissionResultState) {
            RuntimePermissionResultState resultState = (RuntimePermissionResultState) state;

            if (listener == null) {
                throw new RuntimeException("No RuntimePermissionFragmentListener set");
            }

            if (resultState.isGranted) {
                listener.onPermissionGranted(resultState.permission);
            } else {
                listener.onPermissionDenied(resultState.permission);
            }

            viewModel.onRuntimePermissionResultStateReported();
        }
    }

    private void requestPermission(RequestRuntimePermissionState state) {
        requestPermissions(new String[] {state.runtimePermissionRequest.getPermission()}, RequestCode.REQUEST_PERMISSION);

        viewModel.onPermissionRequested(state.runtimePermissionRequest);
    }

    private void showRunTimePermissionRationale(String tag, RuntimePermissionRequest request) {
        PermissionRationaleDialog dialog = findFragment(tag);

        if (dialog == null) {
            dialog = PermissionRationaleDialog.newInstance(request);

            show(dialog, tag);
        }

        dialog.setListener(this);
    }

    @Override
    public void onPermissionRationaleAccepted(@NonNull RuntimePermissionRequest request) {
        viewModel.onPermissionRationaleAccepted(request);
    }

    @Override
    public void onPermissionRationaleDenied(@NonNull RuntimePermissionRequest request) {
        viewModel.onPermissionRationaleDenied(request);
    }
}
