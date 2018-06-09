package nl.erikduisters.pathfinder.ui.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import nl.erikduisters.pathfinder.R;

/**
 * Created by Erik Duisters on 09-06-2018.
 */
public class YesNoMessageDialog extends DialogFragment {
    private final static String KEY_MESSAGE = "Message";

    public interface Listener {
        void onYesClicked();
        void onNoClicked();
    }

    MessageWithTitle message;
    private Listener listener;

    public YesNoMessageDialog() {
    }

    public static YesNoMessageDialog newInstance(MessageWithTitle msg) {
        YesNoMessageDialog dialog = new YesNoMessageDialog();

        dialog.setMessage(msg);

        return dialog;
    }

    protected void setMessage(MessageWithTitle msg) {
        Bundle args = new Bundle();
        args.putParcelable(KEY_MESSAGE, msg);

        setArguments(args);
    }

    public MessageWithTitle getMessage() {
        return message;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();

        if (args == null || !args.containsKey(KEY_MESSAGE)) {
            throw new RuntimeException("You must instantiate a new YesNoMessageDialog using YesNoMessageDialog.newInstance()");
        }

        message = args.getParcelable(KEY_MESSAGE);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(message.titleResId);

        if (message.messageResId > 0) {
            builder.setMessage(message.messageResId);
        } else {
            builder.setMessage(message.message);
        }

        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                onYesClicked();
            }
        });
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                onNoClicked();
            }
        });

        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);

        return dialog;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);

        onNoClicked();
    }

    void onYesClicked() {
        if (listener != null) {
            listener.onYesClicked();
        }
    }

    void onNoClicked() {
        if (listener != null) {
            listener.onNoClicked();
        }
    }
}
