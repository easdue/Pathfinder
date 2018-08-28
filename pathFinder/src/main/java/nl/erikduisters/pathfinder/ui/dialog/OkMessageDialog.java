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

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import nl.erikduisters.pathfinder.R;

/**
 * Created by Erik Duisters on 05-06-2018.
 */
public class OkMessageDialog extends DialogFragment {
    private static final String KEY_MESSAGE = "Message";

    private OkMessageDialogListener listener;

    public interface OkMessageDialogListener {
        void onOkMessageDialogDismiss();
    }

    public OkMessageDialog() {
    }

    public static OkMessageDialog newInstance(MessageWithTitle msg) {
        Bundle args = new Bundle();
        args.putParcelable(KEY_MESSAGE, msg);

        OkMessageDialog dialog = new OkMessageDialog();
        dialog.setArguments(args);

        return dialog;
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        MessageWithTitle msg = getArguments().getParcelable(KEY_MESSAGE);

        if (msg == null) {
            throw new RuntimeException("You must call OkMessageDialog.newInstance() to create a new OkMessageDialog");
        }

        builder.setTitle(msg.titleResId);
        builder.setMessage(msg.getMessage(getContext()));

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (listener != null) {
                    listener.onOkMessageDialogDismiss();
                }
            }
        });

        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);

        return dialog;

    }

    public void setListener(OkMessageDialogListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);

        if (listener != null) {
            listener.onOkMessageDialogDismiss();
        }
    }
}
