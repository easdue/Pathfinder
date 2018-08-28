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
