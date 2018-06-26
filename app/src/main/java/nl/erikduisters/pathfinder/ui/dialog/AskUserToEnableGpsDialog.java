package nl.erikduisters.pathfinder.ui.dialog;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

/**
 * Created by Erik Duisters on 26-06-2018.
 */
public class AskUserToEnableGpsDialog extends PositiveNegativeButtonMessageDialog {
    public interface Listener {
        void onUserWantsToEnableGps();
        void onUserDoesNotWantToEnableGps();
    }

    @Nullable private Listener listener;

    public static AskUserToEnableGpsDialog newInstance(@NonNull MessageWithTitle message, @StringRes int positiveButtonTextResId, @StringRes int negativeButtonTextResId) {
        AskUserToEnableGpsDialog dialog = new AskUserToEnableGpsDialog();

        dialog.setArguments(message, positiveButtonTextResId, negativeButtonTextResId);

        return dialog;
    }

    public void setListener(@Nullable Listener listener) {
        this.listener = listener;
    }

    @Override
    void onPositiveButtonClicked() {
        if (listener != null) {
            listener.onUserWantsToEnableGps();
        }
    }

    @Override
    void onNegativeButtonClicked() {
        if (listener != null) {
            listener.onUserDoesNotWantToEnableGps();
        }
    }
}
