package nl.erikduisters.pathfinder.ui.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

/**
 * Created by Erik Duisters on 09-06-2018.
 */
public class PositiveNegativeButtonMessageDialog extends DialogFragment {
    private static final String KEY_MESSAGE = "Message";
    private static final String KEY_POSITIVE_BUTTON_TEXT_RES_ID = "PositiveButtonTextResId";
    private static final String KEY_NEGATIVE_BUTTON_TEXT_RES_ID = "NegativeButtonTextResId";
    private static final String KEY_TAG = "Tag";

    public interface Listener {
        void onPositiveButtonClicked(@NonNull String tag);
        void onNegativeButtonClicked(@NonNull String tag);
    }

    @Nullable private Listener listener;
    private MessageWithTitle messageWithTitle;
    @StringRes int positiveButtonTextResId;
    @StringRes int negativeButtonTextResId;
    private String tag;

    public PositiveNegativeButtonMessageDialog() {}

    public static PositiveNegativeButtonMessageDialog newInstance(@NonNull MessageWithTitle message,
                                                                  @StringRes int positiveButtonTextResId,
                                                                  @StringRes int negativeButtonTextResId,
                                                                  @NonNull String tag) {
        PositiveNegativeButtonMessageDialog dialog = new PositiveNegativeButtonMessageDialog();
        dialog.setArguments(message, positiveButtonTextResId, negativeButtonTextResId, tag);

        return dialog;
    }

    public void setListener(@Nullable Listener listener) {
        this.listener = listener;
    }

    protected void setArguments(@NonNull MessageWithTitle msg,
                                @StringRes int positiveButtonTextResId,
                                @StringRes int negativeButtonTextResId,
                                @NonNull String tag) {
        Bundle args = new Bundle();
        args.putParcelable(KEY_MESSAGE, msg);
        args.putInt(KEY_POSITIVE_BUTTON_TEXT_RES_ID, positiveButtonTextResId);
        args.putInt(KEY_NEGATIVE_BUTTON_TEXT_RES_ID, negativeButtonTextResId);
        args.putString(KEY_TAG, tag);

        setArguments(args);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();

        if (args == null || !args.containsKey(KEY_MESSAGE) || !args.containsKey(KEY_POSITIVE_BUTTON_TEXT_RES_ID)
                || !args.containsKey(KEY_NEGATIVE_BUTTON_TEXT_RES_ID) || !args.containsKey(KEY_TAG)) {
            throw new IllegalStateException("You must call setArguments() to properly initialize a PositiveNegativeButtonMessageDialog");
        }

        messageWithTitle = args.getParcelable(KEY_MESSAGE);
        positiveButtonTextResId = args.getInt(KEY_POSITIVE_BUTTON_TEXT_RES_ID);
        negativeButtonTextResId = args.getInt(KEY_NEGATIVE_BUTTON_TEXT_RES_ID);
        tag = args.getString(KEY_TAG);
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //noinspection ConstantConditions
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        //noinspection ConstantConditions
        builder.setTitle(messageWithTitle.titleResId);

        if (messageWithTitle.messageResId > 0) {
            builder.setMessage(messageWithTitle.messageResId);
        } else {
            builder.setMessage(messageWithTitle.message);
        }

        builder.setPositiveButton(positiveButtonTextResId, (dialog, id) -> onPositiveButtonClicked(tag));
        builder.setNegativeButton(negativeButtonTextResId, (dialog, id) -> onNegativeButtonClicked(tag));

        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);

        return dialog;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);

        onNegativeButtonClicked(tag);
    }

    void onPositiveButtonClicked(String tag) {
        if (listener != null) {
            listener.onPositiveButtonClicked(tag);
        }
    }

    void onNegativeButtonClicked(String tag) {
        if (listener != null) {
            listener.onNegativeButtonClicked(tag);
        }
    }
}
