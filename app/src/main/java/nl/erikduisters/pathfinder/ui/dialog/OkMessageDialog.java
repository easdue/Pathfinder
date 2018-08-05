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
