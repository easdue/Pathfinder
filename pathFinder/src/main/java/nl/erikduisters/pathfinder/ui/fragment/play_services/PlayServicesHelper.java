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

package nl.erikduisters.pathfinder.ui.fragment.play_services;

import android.annotation.SuppressLint;
import android.support.annotation.IntDef;
import android.support.annotation.StringRes;

import com.google.android.gms.location.LocationRequest;

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
    void tryToResolveUnavailabilityState(@ServiceState int state, GooglePlayServicesAvailabilityCallback callback);

    default boolean isStateUserResolvable(@ServiceState int state) {
        return state != ServiceState.SERVICE_OK && state != ServiceState.SERVICE_INVALID;
    }

    void checkLocationSettings(LocationRequest locationRequest, LocationSettingsCallback callback);
    void tryToCorrectLocationSettings(LocationSettingsCallback callback);

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

    interface GooglePlayServicesAvailabilityCallback {
        void onGooglePlayServicesAvailable();
        void onGooglePlayServicesUnavailable();

    }

    interface LocationSettingsCallback {
        void onLocationSettingsCorrect();
        void onLocationSettingsIncorrect(boolean isResolvable);
    }
}
