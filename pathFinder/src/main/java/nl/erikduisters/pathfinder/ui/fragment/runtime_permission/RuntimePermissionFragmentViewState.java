package nl.erikduisters.pathfinder.ui.fragment.runtime_permission;

/**
 * Created by Erik Duisters on 09-06-2018.
 */

public interface RuntimePermissionFragmentViewState {
    final class RequestRuntimePermissionState implements RuntimePermissionFragmentViewState {
        final RuntimePermissionRequest runtimePermissionRequest;

        RequestRuntimePermissionState(RuntimePermissionRequest runtimePermissionRequest) {
            this.runtimePermissionRequest = runtimePermissionRequest;
        }
    }

    final class RequestingRuntimePermissionState implements RuntimePermissionFragmentViewState {
        final RuntimePermissionRequest runtimePermissionRequest;

        RequestingRuntimePermissionState(RuntimePermissionRequest request) {
            this.runtimePermissionRequest = request;
        }
    }

    final class ShowPermissionRationaleState implements RuntimePermissionFragmentViewState {
        final RuntimePermissionRequest runtimePermissionRequest;

        ShowPermissionRationaleState(RuntimePermissionRequest runtimePermissionRequest) {
            this.runtimePermissionRequest = runtimePermissionRequest;
        }
    }

    final class RuntimePermissionResultState implements RuntimePermissionFragmentViewState {
        final String permission;
        final boolean isGranted;

        RuntimePermissionResultState(String permission, boolean isGranted) {
            this.permission = permission;
            this.isGranted = isGranted;
        }
    }
}
