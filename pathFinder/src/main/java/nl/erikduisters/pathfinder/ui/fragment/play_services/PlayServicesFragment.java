package nl.erikduisters.pathfinder.ui.fragment.play_services;

import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import nl.erikduisters.pathfinder.ui.BaseFragment;
import nl.erikduisters.pathfinder.ui.RequestCode;
import nl.erikduisters.pathfinder.ui.dialog.PositiveNegativeButtonMessageDialog;
import nl.erikduisters.pathfinder.ui.dialog.ProgressDialog;
import nl.erikduisters.pathfinder.ui.fragment.HeadlessFragment;
import nl.erikduisters.pathfinder.ui.fragment.play_services.PlayServicesFragmentViewState.AskUserToResolveUnavailabilityState;
import nl.erikduisters.pathfinder.ui.fragment.play_services.PlayServicesFragmentViewState.ReportPlayServicesAvailabilityState;
import nl.erikduisters.pathfinder.ui.fragment.play_services.PlayServicesFragmentViewState.WaitForPlayServicesUpdateState;
import nl.erikduisters.pathfinder.ui.fragment.play_services.PlayServicesFragmentViewState.WaitingForUserToResolveUnavailabilityState;
import timber.log.Timber;

/**
 * Created by Erik Duisters on 18-06-2018.
 */
public class PlayServicesFragment
        extends BaseFragment<PlayServicesFragmentViewModel>
        implements PlayServicesHelper, HeadlessFragment {

    public interface PlayServicesFragmentListener {
        void onPlayServicesAvailable();
        void onPlayServicesUnavailable();
    }

    private static final String KEY_ASK_USER_TO_RESOLVE_UNAVAILABILITY_DIALOG = "AskUserToResolveUnavailabilityDialog";
    private static final String KEY_WAITING_FOR_PLAY_SERVICES_UPDATE_DIALOG = "WaitingForPlayServicesUpdateDialog";

    private @Nullable PlayServicesFragmentListener listener;
    private static ResolvableApiException currentResolvableApiException;

    public PlayServicesFragment() {}

    public static PlayServicesFragment newInstance() {
        return new PlayServicesFragment();
    }

    public void setListener(@Nullable PlayServicesFragmentListener listener) {
        this.listener = listener;
    }

    @Override
    protected int getLayoutResId() {
        return 0;
    }

    @Override
    protected Class<PlayServicesFragmentViewModel> getViewModelClass() {
        return PlayServicesFragmentViewModel.class;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel.setPlayServicesHelper(this);
        viewModel.checkPlayServicesAvailability();
        viewModel.getViewStateObservable().observe(this, this::render);
    }

    @Override
    public void onDestroy() {
        viewModel.setPlayServicesHelper(null);

        super.onDestroy();
    }

    private void render(@Nullable PlayServicesFragmentViewState viewState) {
        Timber.d("render(viewState == %s)", viewState == null ? "null" : viewState.getClass().getSimpleName());

        if (viewState == null) {
            return;
        }

        if (viewState instanceof AskUserToResolveUnavailabilityState) {
            ShowAskUserToResolveUnavailabilityDialog((AskUserToResolveUnavailabilityState) viewState,
                    KEY_ASK_USER_TO_RESOLVE_UNAVAILABILITY_DIALOG);
        } else {
            dismissDialogFragment(KEY_ASK_USER_TO_RESOLVE_UNAVAILABILITY_DIALOG);
        }

        if (viewState instanceof WaitingForUserToResolveUnavailabilityState) {
            //Do nothing
        }

        if (viewState instanceof WaitForPlayServicesUpdateState) {
            ShowProgressDialog((WaitForPlayServicesUpdateState)viewState, KEY_WAITING_FOR_PLAY_SERVICES_UPDATE_DIALOG);
        } else {
            dismissDialogFragment(KEY_WAITING_FOR_PLAY_SERVICES_UPDATE_DIALOG);
        }

        if (viewState instanceof ReportPlayServicesAvailabilityState) {
            if (listener == null) return;

            if (((ReportPlayServicesAvailabilityState)viewState).googlePlayServicesIsAvailable) {
                listener.onPlayServicesAvailable();
            } else {
                listener.onPlayServicesUnavailable();
            }

            viewModel.onPlayServicesAvailabilityStateReported();
        }

        if (viewState instanceof PlayServicesFragmentViewState.WaitingForLocationSettingsCheckState) {
            //Do nothing
        }

        if (viewState instanceof PlayServicesFragmentViewState.WaitingForLocationSettingsResolutionState) {
            //Do nothing
        }
    }

    private void ShowAskUserToResolveUnavailabilityDialog(AskUserToResolveUnavailabilityState viewState, String tag) {
        PositiveNegativeButtonMessageDialog dialog = findFragment(tag);

        if (dialog == null) {
            dialog = PositiveNegativeButtonMessageDialog.newInstance(viewState.dialogInfo);
            dialog.setCancelable(false);

            show(dialog, tag);
        }

        dialog.setListener(new PositiveNegativeButtonMessageDialog.Listener() {
            @Override
            public void onPositiveButtonClicked(boolean neverAskAgain) {
                viewModel.onUserWantsToResolveUnavailabilityState(neverAskAgain);
            }

            @Override
            public void onNegativeButtonClicked(boolean neverAskAgain) {
                viewModel.onUserDoesNotWantToResolveUnavailabilityState(neverAskAgain);
            }

            @Override
            public void onDialogCancelled() {
                viewModel.onUserDoesNotWantToResolveUnavailabilityState(false);
            }
        });
    }

    private void ShowProgressDialog(WaitForPlayServicesUpdateState state, String tag) {
        ProgressDialog dialog = findFragment(tag);

        if (dialog == null) {
            dialog = ProgressDialog.newInstance(state.dialogProperties);

            show(dialog, tag);
        }

        dialog.setKeyProgressMessage(state.progressMessageResId);

        dialog.setListener(() -> viewModel.onUserDoesNotWantToResolveUnavailabilityState(false));
    }

    @Override
    @ServiceState
    public int getGooglePlayServicesState() {
        int state = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getContext());

        return serviceStateFromInt(state);
    }

    private @ServiceState int serviceStateFromInt(int value) {
        switch (value) {
            case ServiceState.SERVICE_OK:
            case ServiceState.SERVICE_MISSING:
            case ServiceState.SERVICE_UPDATE_REQUIRED:
            case ServiceState.SERVICE_DISABLED:
            case ServiceState.SERVICE_INVALID:
            case ServiceState.SERVICE_UPDATING:
                return value;
            default:
                throw new RuntimeException("serviceStateFromInt called with an unexpected value: " + value);
        }
    }

    @Override
    public void tryToResolveUnavailabilityState(@ServiceState int state, GooglePlayServicesAvailabilityCallback callback) {
        if (!(callback instanceof PlayServicesFragmentViewModel)) {
            throw new IllegalArgumentException("GooglePlayServicesAvailabilityCallback must be implemented by PlayServicesFragmentViewModel");
        }

        Intent intent = GoogleApiAvailability.getInstance().getErrorResolutionIntent(getContext(), state, null);

        if (intent == null) {
            viewModel.onGooglePlayServicesUnavailable();
            return;
        }

        try {
            startActivityForResult(intent, RequestCode.GOOGLEPLAY_ERROR_RESOLUTION_REQUEST);
        } catch (ActivityNotFoundException e) {
            viewModel.onGooglePlayServicesUnavailable();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode) {
            case RequestCode.GOOGLEPLAY_ERROR_RESOLUTION_REQUEST:
                //resultCode is always RESULT_CANCELED
                if (getGooglePlayServicesState() == ServiceState.SERVICE_OK) {
                    viewModel.onGooglePlayServicesAvailable();
                } else {
                    viewModel.onGooglePlayServicesUnavailable();
                }
                break;
            case RequestCode.LOCATION_SETTINGS_RESOLUTION_REQUEST:
                //resultCode is always RESULT_CANCELED
                viewModel.onLocationSettingsCorrect();
        }
    }

    @Override
    public void checkLocationSettings(LocationRequest locationRequest, LocationSettingsCallback callback) {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        //noinspection ConstantConditions
        SettingsClient client = LocationServices.getSettingsClient(getActivity());
        client.checkLocationSettings(builder.build())
                .addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        callback.onLocationSettingsCorrect();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        boolean isResolvable = e instanceof ResolvableApiException;

                        if (isResolvable) {
                            currentResolvableApiException = (ResolvableApiException) e;

                        }

                        callback.onLocationSettingsIncorrect(isResolvable);
                    }
                })
                .addOnCanceledListener(new OnCanceledListener() {
                    @Override
                    public void onCanceled() {
                        callback.onLocationSettingsIncorrect(false);
                    }
                });
    }

    @Override
    public void tryToCorrectLocationSettings(LocationSettingsCallback callback) {
        if (currentResolvableApiException == null) {
            throw new IllegalStateException("currentResolvableApiException == null. Did you call checkLocationSettings()?");
        }

        if (!(callback instanceof PlayServicesFragmentViewModel)) {
            throw new IllegalArgumentException("LocationSettingsCallback must be implemented by PlayServicesFragmentViewModel");
        }

        /* This does not call through to our onActivityResult but only to MainActivity.onActivityResult
           And unfortunately to dialog that is shown does not use colorAccent for the buttons, wtf google!
        try {
            currentResolvableApiException.startResolutionForResult(getActivity(), RequestCode.LOCATION_SETTINGS_RESOLUTION_REQUEST);
        } catch (IntentSender.SendIntentException e1) {
            e1.printStackTrace();

            //https://developer.android.com/training/location/change-location-settings says to ignore this exception
            callback.onLocationSettingsCorrect();
        }
        */
        PendingIntent pi = currentResolvableApiException.getResolution();
        try {
            Bundle bundle = new Bundle();
            bundle.putInt("overrideTheme", 0);
            bundle.putInt("overrideCustomTheme", 0);
            getActivity().startIntentSenderFromFragment(this, pi.getIntentSender(), RequestCode.LOCATION_SETTINGS_RESOLUTION_REQUEST, null, 0, 0, 0, bundle);
        } catch (IntentSender.SendIntentException e) {
            //https://developer.android.com/training/location/change-location-settings says to ignore this exception
            callback.onLocationSettingsCorrect();
        }
    }
}
