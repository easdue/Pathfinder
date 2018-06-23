package nl.erikduisters.pathfinder.ui.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

/**
 * Created by Erik Duisters on 09-06-2018.
 */
public class PositiveNegativeButtonMessageDialog extends DialogFragment {
    private final static String KEY_MESSAGE = "Message";
    private static final String KEY_POSITIVE_BUTTON_TEXT_RES_ID = "PositiveButtonTextResId";
    private static final String KEY_NEGATIVE_BUTTON_TEXT_RES_ID = "NegativeButtonTextResId";

    public interface Listener {
        void onPositiveButtonClicked();
        void onNegativeButtonClicked();
    }

    private Listener listener;

    public PositiveNegativeButtonMessageDialog() {
    }

    public static PositiveNegativeButtonMessageDialog newInstance(@NonNull MessageWithTitle msg,
                                                                  @StringRes int positiveButtonTextResId,
                                                                  @StringRes int negativeButtonTextResId) {
        PositiveNegativeButtonMessageDialog dialog = new PositiveNegativeButtonMessageDialog();

        dialog.setArguments(msg, positiveButtonTextResId, negativeButtonTextResId);

        return dialog;
    }

    protected void setArguments(@NonNull MessageWithTitle msg, @StringRes int positiveButtonTextResId, @StringRes int negativeButtonTextResId) {
        Bundle args = new Bundle();
        args.putParcelable(KEY_MESSAGE, msg);
        args.putInt(KEY_POSITIVE_BUTTON_TEXT_RES_ID, positiveButtonTextResId);
        args.putInt(KEY_NEGATIVE_BUTTON_TEXT_RES_ID, negativeButtonTextResId);

        setArguments(args);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();

        if (args == null || !args.containsKey(KEY_MESSAGE) || !args.containsKey(KEY_POSITIVE_BUTTON_TEXT_RES_ID)
                || !args.containsKey(KEY_NEGATIVE_BUTTON_TEXT_RES_ID)) {
            throw new RuntimeException("You must instantiate a new YesNoMessageDialog using YesNoMessageDialog.newInstance()");
        }

        MessageWithTitle message = args.getParcelable(KEY_MESSAGE);
        int positiveButtonTextResId = args.getInt(KEY_POSITIVE_BUTTON_TEXT_RES_ID);
        int negativeButtonTextResId = args.getInt(KEY_NEGATIVE_BUTTON_TEXT_RES_ID);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(message.titleResId);

        if (message.messageResId > 0) {
            builder.setMessage(message.messageResId);
        } else {
            builder.setMessage(message.message);
        }

        builder.setPositiveButton(positiveButtonTextResId, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                onPositiveButtonClicked();
            }
        });
        builder.setNegativeButton(negativeButtonTextResId, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                onNegativeButtonClicked();
            }
        });

        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);

        return dialog;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);

        onNegativeButtonClicked();
    }

    void onPositiveButtonClicked() {
        if (listener != null) {
            listener.onPositiveButtonClicked();
        }
    }

    void onNegativeButtonClicked() {
        if (listener != null) {
            listener.onNegativeButtonClicked();
        }
    }
}
