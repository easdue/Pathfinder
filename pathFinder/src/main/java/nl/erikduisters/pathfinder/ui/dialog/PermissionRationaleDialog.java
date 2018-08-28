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

package nl.erikduisters.pathfinder.ui.dialog;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import nl.erikduisters.pathfinder.R;
import nl.erikduisters.pathfinder.ui.fragment.runtime_permission.RuntimePermissionRequest;

/**
 * Created by Erik Duisters on 09-06-2018.
 */

public class PermissionRationaleDialog extends PositiveNegativeButtonMessageDialog {
    private static final String KEY_PERMISSION_REQUEST = "PermissionRequest";
    public interface Listener {
        void onPermissionRationaleAccepted(@NonNull RuntimePermissionRequest request);
        void onPermissionRationaleDenied(@NonNull RuntimePermissionRequest request);
    }

    private Listener listener;
    private RuntimePermissionRequest request;

    public PermissionRationaleDialog() {}

    public static PermissionRationaleDialog newInstance(@NonNull RuntimePermissionRequest request) {
        PermissionRationaleDialog dialog = new PermissionRationaleDialog();

        PositiveNegativeButtonMessageDialog.DialogInfo.Builder builder = new DialogInfo.Builder();
        builder.withMessageWithTitle(request.getPermissionRationaleMessage())
                .withShowNeverAskAgain(false)
                .withPositiveButtonLabelResId(R.string.yes)
                .withNegativeButtonLabelResId(R.string.no)
                .withCancellable(true);

        dialog.setArguments(builder.build());

        Bundle args = dialog.getArguments();
        if (args == null) {
            args = new Bundle();
        }

        args.putParcelable(KEY_PERMISSION_REQUEST, request);

        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            request = getArguments().getParcelable(KEY_PERMISSION_REQUEST);
        }
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        listener.onPermissionRationaleDenied(request);
    }

    @Override
    void onPositiveButtonClicked() {
        if (listener != null) {
            listener.onPermissionRationaleAccepted(request);
        }
    }

    @Override
    void onNegativeButtonClicked() {
        if (listener != null) {
            listener.onPermissionRationaleDenied(request);
        }
    }
}
