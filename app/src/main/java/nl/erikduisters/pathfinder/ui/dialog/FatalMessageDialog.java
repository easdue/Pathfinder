package nl.erikduisters.pathfinder.ui.dialog;

import android.app.Dialog;
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

        StringBuilder sb = new StringBuilder();
        if (msg.messageResId > 0) {
            sb.append(getString(msg.messageResId));
        } else {
            sb.append(msg.message);
        }

        builder.setMessage(sb.toString());

        builder.setPositiveButton(R.string.ok, (dialog, id) -> {
            if (listener != null) {
                listener.onFatalMessageDialogDismissed();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        this.setCancelable(false);					/* This is the only way to prevent the dialog from being dismissed when pressing the back button */
        return dialog;

    }

    public void setListener(FatalMessageDialogListener listener) {
        this.listener = listener;
    }
}
