package nl.erikduisters.pathfinder.ui.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
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
    public interface Listener{
        void onProgressDialogDismissed();
    }

    private static final String KEY_PROPERTIES = "DialogTitle";
    private static final String KEY_PROGRESS = "Progress";
    private static final String KEY_PROGRESS_MESSAGE = "Message";

    @BindView(R.id.pd_progressBar) ProgressBar progressBar;
    @BindView(R.id.pd_message) TextView message;

    private int currentProgress;
    private @StringRes int currentProgressMessageResId;
    @Nullable private Listener listener;

    public static ProgressDialog newInstance(@NonNull Properties properties) {
        ProgressDialog dialog = new ProgressDialog();

        Bundle args = new Bundle();
        args.putParcelable(KEY_PROPERTIES, properties);

        dialog.setArguments(args);

        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        currentProgress = -1;

        if (savedInstanceState != null && savedInstanceState.containsKey(KEY_PROGRESS)) {
            currentProgress = savedInstanceState.getInt(KEY_PROGRESS);
            currentProgressMessageResId = savedInstanceState.getInt(KEY_PROGRESS_MESSAGE);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (currentProgress >= 0) {
            outState.putInt(KEY_PROGRESS, currentProgress);
            outState.putInt(KEY_PROGRESS_MESSAGE, currentProgressMessageResId);
        }
    }

    public void setListener(@Nullable Listener listener) {
        this.listener = listener;
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        @SuppressWarnings("ConstantConditions")
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        Bundle args = getArguments();

        if (args == null || !args.containsKey(KEY_PROPERTIES)) {
            throw new RuntimeException("You must instantiate a new ProgressDialog by calling newInstance()");
        }

        Properties properties = args.getParcelable(KEY_PROPERTIES);

        if (properties == null) {
            throw new RuntimeException("Properties could not recreated from parcel");
        }

        int layoutResId = properties.showHorizontalProgressBar ? R.layout.horizontal_progressbar_dialog :
                R.layout.indeterminate_progressbar_dialog;

        View view = View.inflate(getContext(), layoutResId, null);

        ButterKnife.bind(this, view);

        builder.setView(view);

        if (properties.positiveButtonTextResId > 0) {
            builder.setPositiveButton(properties.positiveButtonTextResId, (dialog, which) -> {
                if (listener != null) {
                    listener.onProgressDialogDismissed();
                }
            });
        }

        Dialog dialog = builder.create();

        if (properties.titleResId > 0) {
            dialog.setTitle(properties.titleResId);
        }

        setProgressAndMessage(currentProgress, currentProgressMessageResId);

        dialog.setCanceledOnTouchOutside(false);
        this.setCancelable(properties.isCancelable);

        return dialog;
    }

    public void setKeyProgressMessage(@StringRes int progressMessageResId) {
        setProgressAndMessage(0, progressMessageResId);
    }

    public void setProgressAndMessage(int progress, @StringRes int progressMessageResId) {
        if (progressBar == null) {
            currentProgress = progress;
            currentProgressMessageResId = progressMessageResId;

            return;
        }

        currentProgress = progress;
        currentProgressMessageResId = progressMessageResId;

        progressBar.setProgress(currentProgress);

        if (currentProgressMessageResId == 0) {
            this.message.setText("");
        } else {
            this.message.setText(currentProgressMessageResId);
        }
    }

    public static class Properties implements Parcelable {
        private final @StringRes int titleResId;
        private final boolean showHorizontalProgressBar;
        private final boolean progressBarIsIndeterminate;
        private final @StringRes int positiveButtonTextResId;
        private final boolean isCancelable;

        public Properties(@StringRes int titleResId, boolean showHorizontalProgressBar,
                          boolean progressBarIsIndeterminate, @StringRes int positiveButtonTextResId, boolean isCancelable) {
            this.titleResId = titleResId;
            this.showHorizontalProgressBar = showHorizontalProgressBar;
            this.progressBarIsIndeterminate = progressBarIsIndeterminate;
            this.positiveButtonTextResId = positiveButtonTextResId;
            this.isCancelable = isCancelable;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.titleResId);
            dest.writeByte(this.showHorizontalProgressBar ? (byte) 1 : (byte) 0);
            dest.writeByte(this.progressBarIsIndeterminate ? (byte) 1 : (byte) 0);
            dest.writeInt(this.positiveButtonTextResId);
            dest.writeByte(this.isCancelable ? (byte) 1 : (byte) 0);
        }

        Properties(Parcel in) {
            this.titleResId = in.readInt();
            this.showHorizontalProgressBar = in.readByte() != 0;
            this.progressBarIsIndeterminate = in.readByte() != 0;
            this.positiveButtonTextResId = in.readInt();
            this.isCancelable = in.readByte() != 0;
        }

        public static final Parcelable.Creator<Properties> CREATOR = new Parcelable.Creator<Properties>() {
            @Override
            public Properties createFromParcel(Parcel source) {
                return new Properties(source);
            }

            @Override
            public Properties[] newArray(int size) {
                return new Properties[size];
            }
        };
    }
}
