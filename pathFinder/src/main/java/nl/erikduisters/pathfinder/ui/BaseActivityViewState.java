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

package nl.erikduisters.pathfinder.ui;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.annotation.Nullable;

import nl.erikduisters.pathfinder.ui.dialog.PositiveNegativeButtonMessageDialog;
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

    //TODO: Make this a common class that can be used by all activities/fragments
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
        final @NonNull PositiveNegativeButtonMessageDialog.DialogInfo dialogInfo;

        ShowPositiveNegativeDialogState(@NonNull MyMenu optionsMenu,
                                        @NonNull PositiveNegativeButtonMessageDialog.DialogInfo dialogInfo) {
            super(optionsMenu);

            this.dialogInfo = dialogInfo;
        }
    }

    class RetryRetryableMapDownloadsState extends SetOptionsMenuState {
        RetryRetryableMapDownloadsState(MyMenu optionsMenu) {
            super(optionsMenu);
        }
    }
}
