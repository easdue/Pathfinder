package nl.erikduisters.pathfinder.ui.fragment.play_services_availability;

import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import nl.erikduisters.pathfinder.ui.dialog.MessageWithTitle;
import nl.erikduisters.pathfinder.ui.dialog.ProgressDialog;

/**
 * Created by Erik Duisters on 18-06-2018.
 */
public interface PlayServicesAvailabilityFragmentViewState {
    final class AskUserToResolveUnavailabilityState implements PlayServicesAvailabilityFragmentViewState {
        @NonNull MessageWithTitle messageWithTitle;
        @StringRes int positiveButtonTextResId;
        @StringRes int negativeButtonTextResId;

        private AskUserToResolveUnavailabilityState(Builder builder) {
            this.messageWithTitle = builder.messageWithTitle;
            this.positiveButtonTextResId = builder.positiveButtonTextResId;
            this.negativeButtonTextResId = builder.negativeButtonTextResId;
        }

        static class Builder {
            private @NonNull MessageWithTitle messageWithTitle;
            private @StringRes int positiveButtonTextResId;
            private @StringRes int negativeButtonTextResId;

            Builder setMessageWithTitle(@NonNull MessageWithTitle messageWithTitle) {
                this.messageWithTitle = messageWithTitle;
                return this;
            }

            Builder setPositiveButtonTextResId(@StringRes int positiveButtonTextResId) {
                this.positiveButtonTextResId = positiveButtonTextResId;
                return this;
            }

            Builder setNegativeButtonTextResId(@StringRes int negativeButtonTextResId) {
                this.negativeButtonTextResId = negativeButtonTextResId;
                return this;
            }

            AskUserToResolveUnavailabilityState build() {
                return new AskUserToResolveUnavailabilityState(this);
            }
        }
    }

    final class WaitingForUserToResolveUnavailabilityState implements PlayServicesAvailabilityFragmentViewState {
    }

    final class WaitForPlayServicesUpdateState implements PlayServicesAvailabilityFragmentViewState {
        @NonNull final ProgressDialog.Properties dialogProperties;
        final @StringRes int progressMessageResId;

        WaitForPlayServicesUpdateState(@NonNull ProgressDialog.Properties properties, @StringRes int progressMessageResId) {
            this.dialogProperties = properties;
            this.progressMessageResId = progressMessageResId;
        }
    }

    final class ReportPlayServicesAvailabilityState implements PlayServicesAvailabilityFragmentViewState {
        final boolean googlePlayServicesIsAvailable;

        ReportPlayServicesAvailabilityState(boolean googlePlayServicesIsAvailable) {
            this.googlePlayServicesIsAvailable = googlePlayServicesIsAvailable;
        }
    }
}
