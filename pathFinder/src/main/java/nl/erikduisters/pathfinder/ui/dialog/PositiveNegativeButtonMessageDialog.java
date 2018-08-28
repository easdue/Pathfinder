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
    private static final String KEY_POSITIVE_BUTTON_LABEL_RES_ID = "PositiveButtonLabelResId";
    private static final String KEY_NEGATIVE_BUTTON_LABEL_RES_ID = "NegativeButtonLabelResId";
    private static final String KEY_CANCELLABLE = "Cancellable";

    public interface Listener {
        void onPositiveButtonClicked(boolean neverAskAgain);
        void onNegativeButtonClicked(boolean neverAskAgain);
        //void onDialogCancelled();
    }

    @Nullable private Listener listener;
    private MessageWithTitle messageWithTitle;
    private boolean showNeverAskAgain;
    @StringRes int positiveButtonTextResId;
    @StringRes int negativeButtonTextResId;
    boolean cancellable;

    @BindView(R.id.title) TextView title;
    @BindView(R.id.message) TextView message;
    @BindView(R.id.checkBox) CheckBox checkBox;
    @BindView(R.id.adbb_negativeButton) Button negativeButton;
    @BindView(R.id.adbb_neutralButton) Button neutralButton;
    @BindView(R.id.adbb_positiveButton) Button positiveButton;

    public PositiveNegativeButtonMessageDialog() {}

    public static PositiveNegativeButtonMessageDialog newInstance(@NonNull DialogInfo dialogInfo) {
        PositiveNegativeButtonMessageDialog dialog = new PositiveNegativeButtonMessageDialog();
        dialog.setArguments(dialogInfo);

        return dialog;
    }

    public void setListener(@Nullable Listener listener) {
        this.listener = listener;
    }

    protected void setArguments(@NonNull DialogInfo dialogInfo) {
        Bundle args = new Bundle();
        args.putParcelable(KEY_MESSAGE, dialogInfo.messageWithTitle);
        args.putBoolean(KEY_SHOW_NEVER_ASK_AGAIN, dialogInfo.showNeverAskAgain);
        args.putInt(KEY_POSITIVE_BUTTON_LABEL_RES_ID, dialogInfo.positiveButtonLabelResId);
        args.putInt(KEY_NEGATIVE_BUTTON_LABEL_RES_ID, dialogInfo.negativeButtonLabelResId);
        args.putBoolean(KEY_CANCELLABLE, dialogInfo.cancellable);

        setArguments(args);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();

        if (args == null || !args.containsKey(KEY_MESSAGE) ||
                !args.containsKey(KEY_SHOW_NEVER_ASK_AGAIN) ||
                !args.containsKey(KEY_POSITIVE_BUTTON_LABEL_RES_ID) ||
                !args.containsKey(KEY_NEGATIVE_BUTTON_LABEL_RES_ID) ||
                !args.containsKey(KEY_CANCELLABLE)) {
            throw new IllegalStateException("You must call setArguments() to properly initialize a PositiveNegativeButtonMessageDialog");
        }

        messageWithTitle = args.getParcelable(KEY_MESSAGE);
        showNeverAskAgain = args.getBoolean(KEY_SHOW_NEVER_ASK_AGAIN);
        positiveButtonTextResId = args.getInt(KEY_POSITIVE_BUTTON_LABEL_RES_ID);
        negativeButtonTextResId = args.getInt(KEY_NEGATIVE_BUTTON_LABEL_RES_ID);
        cancellable = args.getBoolean(KEY_CANCELLABLE);

        setStyle(DialogFragment.STYLE_NO_TITLE, 0);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.never_ask_again_dialog, container, false);

        ButterKnife.bind(this, v);

        title.setText(messageWithTitle.titleResId);

        message.setText(messageWithTitle.getMessage(getContext()));

        checkBox.setVisibility(showNeverAskAgain ? View.VISIBLE : View.GONE);

        negativeButton.setText(negativeButtonTextResId);
        negativeButton.setOnClickListener(v1 -> onNegativeButtonClicked());
        positiveButton.setText(positiveButtonTextResId);
        positiveButton.setOnClickListener(v1 -> onPositiveButtonClicked());

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
        //this.setCancelable(cancellable);

        return dialog;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);

        if (listener != null) {
            listener.onNegativeButtonClicked(false);
        }
    }

    void onPositiveButtonClicked() {
        if (listener != null) {
            listener.onPositiveButtonClicked(checkBox.isChecked());
        }
    }

    void onNegativeButtonClicked() {
        if (listener != null) {
            listener.onNegativeButtonClicked(checkBox.isChecked());
        }
    }

    public final static class DialogInfo {
        public @NonNull final MessageWithTitle messageWithTitle;
        public final boolean showNeverAskAgain;
        public @StringRes final int positiveButtonLabelResId;
        public @StringRes final int negativeButtonLabelResId;
        public final boolean cancellable;

        private DialogInfo(Builder builder) {
            messageWithTitle = builder.messageWithTitle;
            showNeverAskAgain = builder.showNeverAskAgain;
            positiveButtonLabelResId = builder.positiveButtonLabelResId;
            negativeButtonLabelResId = builder.negativeButtonLabelResId;
            cancellable = builder.cancellable;
        }


        public static final class Builder {
            private MessageWithTitle messageWithTitle;
            private boolean showNeverAskAgain;
            private int positiveButtonLabelResId;
            private int negativeButtonLabelResId;
            private boolean cancellable;

            public Builder() {
            }

            public Builder withMessageWithTitle(@NonNull MessageWithTitle messageWithTitle) {
                this.messageWithTitle = messageWithTitle;
                return this;
            }

            public Builder withShowNeverAskAgain(boolean showNeverAskAgain) {
                this.showNeverAskAgain = showNeverAskAgain;
                return this;
            }

            public Builder withPositiveButtonLabelResId(@StringRes int positiveButtonLabelResId) {
                this.positiveButtonLabelResId = positiveButtonLabelResId;
                return this;
            }

            public Builder withNegativeButtonLabelResId(@StringRes int negativeButtonLabelResId) {
                this.negativeButtonLabelResId = negativeButtonLabelResId;
                return this;
            }

            public Builder withCancellable(boolean cancellable) {
                this.cancellable = cancellable;
                return this;
            }

            public DialogInfo build() {
                return new DialogInfo(this);
            }
        }
    }
}
