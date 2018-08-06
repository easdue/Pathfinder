package nl.erikduisters.pathfinder.ui.fragment.play_services;

import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import nl.erikduisters.pathfinder.ui.dialog.PositiveNegativeButtonMessageDialog;
import nl.erikduisters.pathfinder.ui.dialog.ProgressDialog;

/**
 * Created by Erik Duisters on 18-06-2018.
 */
public interface PlayServicesFragmentViewState {
    final class AskUserToResolveUnavailabilityState implements PlayServicesFragmentViewState {
        @NonNull final PositiveNegativeButtonMessageDialog.DialogInfo dialogInfo;

        AskUserToResolveUnavailabilityState(PositiveNegativeButtonMessageDialog.DialogInfo dialogInfo) {
            this.dialogInfo = dialogInfo;
        }
    }

    final class WaitingForUserToResolveUnavailabilityState implements PlayServicesFragmentViewState {
    }

    final class WaitForPlayServicesUpdateState implements PlayServicesFragmentViewState {
        @NonNull final ProgressDialog.Properties dialogProperties;
        final @StringRes int progressMessageResId;

        WaitForPlayServicesUpdateState(@NonNull ProgressDialog.Properties properties, @StringRes int progressMessageResId) {
            this.dialogProperties = properties;
            this.progressMessageResId = progressMessageResId;
        }
    }

    final class ReportPlayServicesAvailabilityState implements PlayServicesFragmentViewState {
        final boolean googlePlayServicesIsAvailable;

        ReportPlayServicesAvailabilityState(boolean googlePlayServicesIsAvailable) {
            this.googlePlayServicesIsAvailable = googlePlayServicesIsAvailable;
        }
    }

    final class WaitingForLocationSettingsCheckState implements PlayServicesFragmentViewState {
    }

    final class WaitingForLocationSettingsResolutionState implements PlayServicesFragmentViewState {
    }
}
