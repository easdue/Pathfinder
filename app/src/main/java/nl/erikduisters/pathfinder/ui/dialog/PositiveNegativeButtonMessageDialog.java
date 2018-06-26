package nl.erikduisters.pathfinder.ui.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import nl.erikduisters.pathfinder.R;

/**
 * Created by Erik Duisters on 09-06-2018.
 */
public class PositiveNegativeButtonMessageDialog extends DialogFragment {
    private static final String KEY_MESSAGE = "Message";
    private static final String KEY_SHOW_NEVER_ASK_AGAIN = "ShowNeverAskAGain";
    private static final String KEY_POSITIVE_BUTTON_TEXT_RES_ID = "PositiveButtonTextResId";
    private static final String KEY_NEGATIVE_BUTTON_TEXT_RES_ID = "NegativeButtonTextResId";
    private static final String KEY_TAG = "Tag";

    public interface Listener {
        void onPositiveButtonClicked(@NonNull String tag, boolean neverAskAgain);
        void onNegativeButtonClicked(@NonNull String tag, boolean neverAskAgain);
    }

    @Nullable private Listener listener;
    private MessageWithTitle messageWithTitle;
    private boolean showNeverAskAgain;
    @StringRes int positiveButtonTextResId;
    @StringRes int negativeButtonTextResId;
    private String tag;

    @BindView(R.id.title) TextView title;
    @BindView(R.id.message) TextView message;
    @BindView(R.id.checkBox) CheckBox checkBox;
    @BindView(R.id.adbb_negativeButton) Button negativeButton;
    @BindView(R.id.adbb_neutralButton) Button neutralButton;
    @BindView(R.id.adbb_positiveButton) Button positiveButton;

    public PositiveNegativeButtonMessageDialog() {}

    public static PositiveNegativeButtonMessageDialog newInstance(@NonNull MessageWithTitle message,
                                                                  boolean showNeverAskAgain,
                                                                  @StringRes int positiveButtonTextResId,
                                                                  @StringRes int negativeButtonTextResId,
                                                                  @NonNull String tag) {
        PositiveNegativeButtonMessageDialog dialog = new PositiveNegativeButtonMessageDialog();
        dialog.setArguments(message, showNeverAskAgain, positiveButtonTextResId, negativeButtonTextResId, tag);

        return dialog;
    }

    public void setListener(@Nullable Listener listener) {
        this.listener = listener;
    }

    protected void setArguments(@NonNull MessageWithTitle msg,
                                boolean showNeverAskAgain,
                                @StringRes int positiveButtonTextResId,
                                @StringRes int negativeButtonTextResId,
                                @NonNull String tag) {
        Bundle args = new Bundle();
        args.putParcelable(KEY_MESSAGE, msg);
        args.putBoolean(KEY_SHOW_NEVER_ASK_AGAIN, showNeverAskAgain);
        args.putInt(KEY_POSITIVE_BUTTON_TEXT_RES_ID, positiveButtonTextResId);
        args.putInt(KEY_NEGATIVE_BUTTON_TEXT_RES_ID, negativeButtonTextResId);
        args.putString(KEY_TAG, tag);

        setArguments(args);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();

        if (args == null || !args.containsKey(KEY_MESSAGE) ||
                !args.containsKey(KEY_SHOW_NEVER_ASK_AGAIN) ||
                !args.containsKey(KEY_POSITIVE_BUTTON_TEXT_RES_ID) ||
                !args.containsKey(KEY_NEGATIVE_BUTTON_TEXT_RES_ID) ||
                !args.containsKey(KEY_TAG)) {
            throw new IllegalStateException("You must call setArguments() to properly initialize a PositiveNegativeButtonMessageDialog");
        }

        messageWithTitle = args.getParcelable(KEY_MESSAGE);
        showNeverAskAgain = args.getBoolean(KEY_SHOW_NEVER_ASK_AGAIN);
        positiveButtonTextResId = args.getInt(KEY_POSITIVE_BUTTON_TEXT_RES_ID);
        negativeButtonTextResId = args.getInt(KEY_NEGATIVE_BUTTON_TEXT_RES_ID);
        tag = args.getString(KEY_TAG);

        setStyle(DialogFragment.STYLE_NO_TITLE, 0);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.never_ask_again_dialog, container, false);

        ButterKnife.bind(this, v);

        title.setText(messageWithTitle.titleResId);

        if (messageWithTitle.messageResId > 0) {
            message.setText(messageWithTitle.messageResId);
        } else {
            message.setText(messageWithTitle.message);
        }

        checkBox.setVisibility(showNeverAskAgain ? View.VISIBLE : View.GONE);

        negativeButton.setText(negativeButtonTextResId);
        negativeButton.setOnClickListener(v1 -> onNegativeButtonClicked(tag));
        positiveButton.setText(positiveButtonTextResId);
        positiveButton.setOnClickListener(v1 -> onPositiveButtonClicked(tag));

        negativeButton.setVisibility(View.VISIBLE);
        positiveButton.setVisibility(View.VISIBLE);
        neutralButton.setVisibility(View.GONE);

        return v;
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        dialog.setOnCancelListener(this);
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
            listener.onPositiveButtonClicked(tag, checkBox.isChecked());
        }
    }

    void onNegativeButtonClicked(String tag) {
        if (listener != null) {
            listener.onNegativeButtonClicked(tag, checkBox.isChecked());
        }
    }
}
