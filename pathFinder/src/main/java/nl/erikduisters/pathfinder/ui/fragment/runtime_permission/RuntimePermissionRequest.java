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

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import nl.erikduisters.pathfinder.ui.dialog.MessageWithTitle;

/**
 * Created by Erik Duisters on 09-06-2018.
 */
public class RuntimePermissionRequest implements Parcelable {
    private final @NonNull String permission;
    private final MessageWithTitle permissionRationale;

    public RuntimePermissionRequest(@NonNull String permission, MessageWithTitle permissionRationale) {
        this.permission = permission;
        this.permissionRationale = permissionRationale;
    }

    public @NonNull String getPermission() {
        return permission;
    }

    public MessageWithTitle getPermissionRationaleMessage() {
        return permissionRationale;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.permission);
        dest.writeParcelable(this.permissionRationale, flags);
    }

    private RuntimePermissionRequest(Parcel in) {
        this.permission = in.readString();
        this.permissionRationale = in.readParcelable(MessageWithTitle.class.getClassLoader());
    }

    public static final Creator<RuntimePermissionRequest> CREATOR = new Creator<RuntimePermissionRequest>() {
        @Override
        public RuntimePermissionRequest createFromParcel(Parcel source) {
            return new RuntimePermissionRequest(source);
        }

        @Override
        public RuntimePermissionRequest[] newArray(int size) {
            return new RuntimePermissionRequest[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RuntimePermissionRequest request = (RuntimePermissionRequest) o;

        return permission.equals(request.permission);
    }

    @Override
    public int hashCode() {
        return permission.hashCode();
    }
}
