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
 * @author Erik Duisters
 */
public class FatalMessageDialog extends DialogFragment {
    private static final String KEY_MESSAGE = "Message";

    private FatalMessageDialogListener listener;

    public interface FatalMessageDialogListener {
        void onFatalMessageDialogDismissed();
    }

    public static FatalMessageDialog newInstance(MessageWithTitle msg) {
        FatalMessageDialog d = new FatalMessageDialog();

        Bundle args = new Bundle();
        args.putParcelable(KEY_MESSAGE, msg);
        d.setArguments(args);

        return d;
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        Bundle args = getArguments();
        MessageWithTitle msg = args.getParcelable(KEY_MESSAGE);

        builder.setTitle(msg.titleResId);

        builder.setMessage(msg.getMessage(getContext()));

        builder.setPositiveButton(R.string.ok, (dialog, id) -> {
            if (listener != null) {
                listener.onFatalMessageDialogDismissed();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        //this.setCancelable(false);
        return dialog;

    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);

        listener.onFatalMessageDialogDismissed();
    }

    public void setListener(FatalMessageDialogListener listener) {
        this.listener = listener;
    }
}
