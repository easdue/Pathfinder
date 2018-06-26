package nl.erikduisters.pathfinder.ui.fragment.play_services;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.Nullable;

import javax.inject.Inject;
import javax.inject.Singleton;

import nl.erikduisters.pathfinder.R;
import nl.erikduisters.pathfinder.data.local.GpsManager;
import nl.erikduisters.pathfinder.data.local.PreferenceManager;
import nl.erikduisters.pathfinder.ui.dialog.MessageWithTitle;
import nl.erikduisters.pathfinder.ui.dialog.ProgressDialog;
import nl.erikduisters.pathfinder.ui.fragment.play_services.PlayServicesFragmentViewState.AskUserToResolveUnavailabilityState;
import nl.erikduisters.pathfinder.ui.fragment.play_services.PlayServicesFragmentViewState.ReportPlayServicesAvailabilityState;
import nl.erikduisters.pathfinder.ui.fragment.play_services.PlayServicesFragmentViewState.WaitForPlayServicesUpdateState;
import nl.erikduisters.pathfinder.ui.fragment.play_services.PlayServicesFragmentViewState.WaitingForLocationSettingsCheckState;
import nl.erikduisters.pathfinder.ui.fragment.play_services.PlayServicesFragmentViewState.WaitingForLocationSettingsResolutionState;
import nl.erikduisters.pathfinder.ui.fragment.play_services.PlayServicesFragmentViewState.WaitingForUserToResolveUnavailabilityState;
import nl.erikduisters.pathfinder.ui.fragment.play_services.PlayServicesHelper.ServiceState;
import nl.erikduisters.pathfinder.util.MainThreadExecutor;
import timber.log.Timber;

/**
 * Created by Erik Duisters on 18-06-2018.
 */

@Singleton
public class PlayServicesFragmentViewModel
        extends ViewModel
        implements PlayServicesHelper.GooglePlayServicesAvailabilityCallback, PlayServicesHelper.LocationSettingsCallback {
    private final MutableLiveData<PlayServicesFragmentViewState> viewStateObservable;
    private @Nullable PlayServicesHelper playServicesHelper;
    private @ServiceState int currentServiceState;
    private final MainThreadExecutor mainThreadExecutor;
    private final GpsManager gpsManager;
    private final PreferenceManager preferenceManager;
    private Runnable checkUpdateStateRunnable;

    @Inject
    PlayServicesFragmentViewModel(MainThreadExecutor mainThreadExecutor, GpsManager gpsManager, PreferenceManager preferenceManager) {
        Timber.d("New PlayServicesAvailabilityFragmentViewModel created");
        viewStateObservable = new MutableLiveData<>();
        this.mainThreadExecutor = mainThreadExecutor;
        this.gpsManager = gpsManager;
        this.preferenceManager = preferenceManager;
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        //Don't leak PlayServicesHelper
        playServicesHelper = null;
    }

    LiveData<PlayServicesFragmentViewState> getViewStateObservable() {
        return viewStateObservable;
    }

    void setPlayServicesHelper(@Nullable PlayServicesHelper helper) {
        this.playServicesHelper = helper;
    }

    public void checkPlayServicesAvailability() {
        if (playServicesHelper == null) {
            throw new RuntimeException("You have to call setPlayServicesHelper() before using any other function of this class");
        }

        @ServiceState int serviceState = playServicesHelper.getGooglePlayServicesState();
        boolean askToResolve = preferenceManager.askToResolvePlayServicesUnavailability();

        if (serviceState == ServiceState.SERVICE_OK) {
            onGooglePlayServicesAvailable();
        } else if (!alreadyHandlingUnavailability()) {
            if (askToResolve && playServicesHelper.isStateUserResolvable(serviceState)) {
                MessageWithTitle message =
                        new MessageWithTitle(playServicesHelper.getDialogTitle(serviceState), playServicesHelper.getDialogMessage(serviceState));

                PlayServicesFragmentViewState.AskUserToResolveUnavailabilityState.Builder builder =
                        new PlayServicesFragmentViewState.AskUserToResolveUnavailabilityState.Builder()
                                .setMessageWithTitle(message)
                                .setShowNeverAskAgain(true)
                                .setNegativeButtonTextResId(playServicesHelper.getDialogNegativeButtonText(serviceState))
                                .setPositiveButtonTextResId(playServicesHelper.getDialogPositiveButtonText(serviceState));

                viewStateObservable.setValue(builder.build());
            } else {
                //TODO: Show a snackbar informing the user that GooglePlayservices are not available
                reportPlayservicesAvailabilityState(false);
            }
        }

        currentServiceState = serviceState;
    }

    private boolean alreadyHandlingUnavailability() {
        PlayServicesFragmentViewState currentState = viewStateObservable.getValue();

        return currentState instanceof AskUserToResolveUnavailabilityState ||
               currentState instanceof WaitingForUserToResolveUnavailabilityState ||
               currentState instanceof WaitForPlayServicesUpdateState;
    }

    private void reportPlayservicesAvailabilityState(boolean available) {
        viewStateObservable.setValue(new ReportPlayServicesAvailabilityState(available));
    }

    void onPlayServicesAvailabilityStateReported() {
        viewStateObservable.setValue(null);
    }

    void onUserWantsToResolveUnavailabilityState(boolean neverAskAgain) {
        handleNeverAskAgain(neverAskAgain);

        if (playServicesHelper == null) {
            throw new RuntimeException("You have to call setPlayServicesHelper() before using any other function of this class");
        }

        if (currentServiceState != ServiceState.SERVICE_UPDATING) {
            playServicesHelper.tryToResolveUnavailabilityState(currentServiceState, this);
            viewStateObservable.setValue(new WaitingForUserToResolveUnavailabilityState());
        } else {
            ProgressDialog.Properties properties =
                    new ProgressDialog.Properties(0, false, true,
                            android.R.string.cancel, false);

            viewStateObservable.setValue(new WaitForPlayServicesUpdateState(properties, R.string.play_services_waiting_for_play_services_update));

            scheduleCheckUpdateStateRunnable();
        }
    }

    private void scheduleCheckUpdateStateRunnable() {
        Timber.d("scheduleCheckUpdateStateRunnable");

        if (checkUpdateStateRunnable == null) {
            checkUpdateStateRunnable = this::checkUpdateState;
        }

        mainThreadExecutor.executeDelayed(checkUpdateStateRunnable, 1000);
    }

    private void checkUpdateState() {
        Timber.d("checkUpdateState");

        if (playServicesHelper != null) {
            if (playServicesHelper.getGooglePlayServicesState() != ServiceState.SERVICE_UPDATING) {
                checkUpdateStateRunnable = null;
                viewStateObservable.setValue(null);

                checkPlayServicesAvailability();

                return;
            }
        }

        scheduleCheckUpdateStateRunnable();
    }

    void onUserDoesNotWantToResolveUnavailabilityState(boolean neverAskAgain) {
        handleNeverAskAgain(neverAskAgain);
        reportPlayservicesAvailabilityState(false);
    }

    private void handleNeverAskAgain(boolean neverAskAgain) {
        if (neverAskAgain) {
            preferenceManager.setAskToResolvePlayServicesUnavailability(false);
        }
    }

    @Override
    public void onGooglePlayServicesAvailable() {
        if (playServicesHelper == null) {
            throw new RuntimeException("You have to call setPlayServicesHelper() before using any other function of this class");
        }

        playServicesHelper.checkLocationSettings(gpsManager.getLocationRequest(), this);
        viewStateObservable.setValue(new WaitingForLocationSettingsCheckState());
    }

    @Override
    public void onGooglePlayServicesUnavailable() {
        if (checkUpdateStateRunnable != null) {
            mainThreadExecutor.cancelDelayed(checkUpdateStateRunnable);

            checkUpdateStateRunnable = null;
        }

        reportPlayservicesAvailabilityState(false);
    }

    @Override
    public void onLocationSettingsCorrect() {
        reportPlayservicesAvailabilityState(true);
    }

    @Override
    public void onLocationSettingsIncorrect(boolean isResolvable) {
        if (playServicesHelper == null) {
            throw new RuntimeException("You have to call setPlayServicesHelper() before using any other function of this class");
        }

        if (isResolvable && viewStateObservable.getValue() instanceof WaitingForLocationSettingsCheckState) {
            playServicesHelper.tryToCorrectLocationSettings(this);
            viewStateObservable.setValue(new WaitingForLocationSettingsResolutionState());
        } else {
            reportPlayservicesAvailabilityState(true);
        }
    }
}
