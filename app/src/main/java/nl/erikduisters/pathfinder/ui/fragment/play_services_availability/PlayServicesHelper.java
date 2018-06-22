package nl.erikduisters.pathfinder.ui.fragment.play_services_availability;

import android.annotation.SuppressLint;
import android.support.annotation.IntDef;
import android.support.annotation.StringRes;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import nl.erikduisters.pathfinder.R;

/**
 * Created by Erik Duisters on 18-06-2018.
 */
public interface PlayServicesHelper {
    @IntDef({ServiceState.SERVICE_OK, ServiceState.SERVICE_MISSING, ServiceState.SERVICE_DISABLED,
             ServiceState.SERVICE_UPDATE_REQUIRED, ServiceState.SERVICE_INVALID, ServiceState.SERVICE_UPDATING})
    @Retention(RetentionPolicy.SOURCE)
    @interface ServiceState {
        int SERVICE_OK = 0;
        int SERVICE_MISSING = 1;
        int SERVICE_UPDATE_REQUIRED = 2;
        int SERVICE_DISABLED = 3;
        int SERVICE_INVALID = 9;
        int SERVICE_UPDATING = 18;
    }

    @ServiceState int getGooglePlayServicesState();
    void tryToResolveUnavailabilityState(@ServiceState int state);

    default boolean isStateUserResolvable(@ServiceState int state) {
        return state != ServiceState.SERVICE_OK && state != ServiceState.SERVICE_INVALID;
    }

    @SuppressLint("SwitchIntDef")
    default @StringRes int getDialogTitle(@ServiceState int state) {
        switch (state) {
            case ServiceState.SERVICE_MISSING:
                return R.string.play_services_missing_title;
            case ServiceState.SERVICE_UPDATE_REQUIRED:
                return R.string.play_services_update_required_title;
            case ServiceState.SERVICE_DISABLED:
                return R.string.play_services_disabled_title;
            case ServiceState.SERVICE_UPDATING:
                return R.string.play_services_updating_title;
            default:
                throw new IllegalArgumentException("You cannot call getDialogTitle for a state that is not user resolvable");
        }
    }

    @SuppressLint("SwitchIntDef")
    default @StringRes int getDialogMessage(@ServiceState int state) {
        switch (state) {
            case ServiceState.SERVICE_MISSING:
                return R.string.play_services_missing_message;
            case ServiceState.SERVICE_UPDATE_REQUIRED:
                return R.string.play_services_update_required_message;
            case ServiceState.SERVICE_DISABLED:
                return R.string.play_services_disabled_message;
            case ServiceState.SERVICE_UPDATING:
                return R.string.play_services_updating_message;
            default:
                throw new IllegalArgumentException("You cannot call getDialogMessage for a state that is not user resolvable");
        }
    }

    @SuppressLint("SwitchIntDef")
    default @StringRes int getDialogPositiveButtonText(@ServiceState int state) {
        switch (state) {
            case ServiceState.SERVICE_MISSING:
                return R.string.play_services_missing_positive_button_text;
            case ServiceState.SERVICE_UPDATE_REQUIRED:
                return R.string.play_services_update_required_positive_button_text;
            case ServiceState.SERVICE_DISABLED:
                return R.string.play_services_disabled_positive_button_text;
            case ServiceState.SERVICE_UPDATING:
                return R.string.play_services_updating_positive_button_text;
            default:
                throw new IllegalArgumentException("You cannot call getDialogPositiveButtonText for a state that is not user resolvable");
        }
    }

    @SuppressLint("SwitchIntDef")
    default @StringRes int getDialogNegativeButtonText(@ServiceState int state) {
        switch (state) {
            case ServiceState.SERVICE_MISSING:
                return R.string.play_services_missing_negative_button_text;
            case ServiceState.SERVICE_UPDATE_REQUIRED:
                return R.string.play_services_update_required_negative_button_text;
            case ServiceState.SERVICE_DISABLED:
                return R.string.play_services_disabled_negative_button_text;
            case ServiceState.SERVICE_UPDATING:
                return R.string.play_services_updating_negative_button_text;
            default:
                throw new IllegalArgumentException("You cannot call getDialogNegativeButtonText for a state that is not user resolvable");
        }
    }
}
