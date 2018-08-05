package nl.erikduisters.pathfinder.ui;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.annotation.Nullable;

import nl.erikduisters.pathfinder.ui.dialog.MessageWithTitle;
import nl.erikduisters.pathfinder.util.menu.MyMenu;

/**
 * Created by Erik Duisters on 30-07-2018.
 */
public interface BaseActivityViewState {
    class SetOptionsMenuState implements BaseActivityViewState {
        final MyMenu optionsMenu;

        SetOptionsMenuState(MyMenu optionsMenu) {
            this.optionsMenu = optionsMenu;
        }
    }

    class DisplayMessageState implements BaseActivityViewState {
        @IntDef({DisplayDuration.SHORT, DisplayDuration.LONG})
        @Retention(RetentionPolicy.SOURCE)
        @interface DisplayDuration {
            int SHORT = 0;
            int LONG = 1;
        }

        private final @StringRes int messageResId;
        private final @Nullable Object[] args;
        final @DisplayDuration int displayDuration;

        DisplayMessageState(@DisplayDuration int displayDuration, @StringRes int messageResId) {
            this(displayDuration, messageResId, (Object[]) null);
        }

        DisplayMessageState(@DisplayDuration int displayDuration, @StringRes int messageResId, @Nullable Object... args) {
            this.displayDuration = displayDuration;
            this.messageResId = messageResId;
            this.args = args;
        }

        String getMessage(Context context) {
            if (args == null) {
                return context.getString(messageResId);
            } else {
                return context.getString(messageResId, (Object[]) args);
            }
        }
    }

    class StartActivityState implements BaseActivityViewState {
        private @NonNull final String action;

        StartActivityState(@NonNull String action) {
            this.action = action;
        }

        Intent getIntent() {
            return new Intent(action);
        }
    }

    class ShowPositiveNegativeDialogState extends SetOptionsMenuState {
        final @NonNull MessageWithTitle messageWithTitle;
        final boolean showNeverAskAgain;
        final @StringRes int positiveButtonTextResId;
        final @StringRes int negativeButtonTextResId;
        final @NonNull String tag;

        ShowPositiveNegativeDialogState(@NonNull MyMenu optionsMenu,
                                        @NonNull MessageWithTitle messageWithTitle,
                                        boolean showNeverAskAgain,
                                        @StringRes int positiveBottonTextResId,
                                        @StringRes int negativeBottonTextResId,
                                        @NonNull String tag) {
            super(optionsMenu);

            this.messageWithTitle = messageWithTitle;
            this.showNeverAskAgain = showNeverAskAgain;
            this.positiveButtonTextResId = positiveBottonTextResId;
            this.negativeButtonTextResId = negativeBottonTextResId;
            this.tag = tag;
        }
    }

    class RetryRetryableMapDownloadsState extends SetOptionsMenuState {
        RetryRetryableMapDownloadsState(MyMenu optionsMenu) {
            super(optionsMenu);
        }
    }
}