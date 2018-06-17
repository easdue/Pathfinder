package nl.erikduisters.pathfinder.ui.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import nl.erikduisters.pathfinder.R;

/**
 * Created by Erik Duisters on 16-06-2018.
 */

public class ProgressDialog extends DialogFragment {
    private static final String KEY_TITLE = "Title";
    private static final String KEY_PROGRESS = "Progress";
    private static final String KEY_MESSAGE = "Message";

    @BindView(R.id.pd_progressBar) ProgressBar progressBar;
    @BindView(R.id.pd_message) TextView message;

    public static ProgressDialog newInstance(@StringRes int title, int progress, @StringRes int message) {
        ProgressDialog dialog = new ProgressDialog();

        Bundle args = new Bundle();
        args.putInt(KEY_TITLE, title);
        args.putInt(KEY_PROGRESS, progress);
        args.putInt(KEY_MESSAGE, message);

        dialog.setArguments(args);

        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        View view = View.inflate(getContext(), R.layout.progress_dialog, null);

        ButterKnife.bind(this, view);

        builder.setView(view);

        Dialog dialog = builder.create();

        Bundle args = getArguments();

        dialog.setTitle(args.getInt(KEY_TITLE));

        dialog.setCanceledOnTouchOutside(false);

        setProgressAndMessage(args.getInt(KEY_PROGRESS), args.getInt(KEY_MESSAGE));

        this.setCancelable(false);					/* This is the only way to prevent the dialog from being dismissed when pressing the back button */

        return dialog;
    }

    public void setProgressAndMessage(final int progress, final @StringRes int message) {
        if (progressBar == null) {
            //Android did not create us yet
            return;
        }

        progressBar.setProgress(progress);

        if (message != 0) {
            this.message.setText(getString(message));
        }
    }
}
