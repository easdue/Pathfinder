package nl.erikduisters.pathfinder.ui.fragment.play_services_availability;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.Nullable;

import javax.inject.Inject;
import javax.inject.Singleton;

import nl.erikduisters.pathfinder.R;
import nl.erikduisters.pathfinder.ui.dialog.MessageWithTitle;
import nl.erikduisters.pathfinder.ui.dialog.ProgressDialog;
import nl.erikduisters.pathfinder.ui.fragment.play_services_availability.PlayServicesAvailabilityFragmentViewState.ReportPlayServicesAvailabilityState;
import nl.erikduisters.pathfinder.ui.fragment.play_services_availability.PlayServicesAvailabilityFragmentViewState.WaitForPlayServicesUpdateState;
import nl.erikduisters.pathfinder.ui.fragment.play_services_availability.PlayServicesAvailabilityFragmentViewState.WaitingForUserToResolveUnavailabilityState;
import nl.erikduisters.pathfinder.ui.fragment.play_services_availability.PlayServicesHelper.ServiceState;
import nl.erikduisters.pathfinder.util.MainThreadExecutor;
import timber.log.Timber;

/**
 * Created by Erik Duisters on 18-06-2018.
 */
@Singleton
public class PlayServicesAvailabilityFragmentViewModel extends ViewModel {
    private final MutableLiveData<PlayServicesAvailabilityFragmentViewState> viewStateObservable;
    private @Nullable PlayServicesHelper playServicesHelper;
    private @ServiceState int currentServiceState;
    private final MainThreadExecutor mainThreadExecutor;
    private Runnable checkUpdateStateRunnable;

    @Inject
    PlayServicesAvailabilityFragmentViewModel(MainThreadExecutor mainThreadExecutor) {
        Timber.d("New PlayServicesAvailabilityFragmentViewModel created");
        viewStateObservable = new MutableLiveData<>();
        this.mainThreadExecutor = mainThreadExecutor;
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        //Don't leak PlayServicesHelper
        playServicesHelper = null;
    }

    LiveData<PlayServicesAvailabilityFragmentViewState> getViewStateObservable() {
        return viewStateObservable;
    }

    void setPlayServicesHelper(@Nullable PlayServicesHelper helper) {
        Timber.d("setPlayServicesHelper(%s)", helper == null ? "null" : "not null");

        this.playServicesHelper = helper;
    }

    /**
     * Initiates a Google Play Services availability check<br>
     * <b>Note:</b> Must be called from onResume()
     */
    public void checkPlayServicesAvailability() {
        if (playServicesHelper == null) {
            throw new RuntimeException("You have to call setPlayServicesHelper() before using any other function of this class");
        }

        @ServiceState int serviceState = playServicesHelper.getGooglePlayServicesState();

        if (serviceState == ServiceState.SERVICE_OK) {
            reportPlayservicesAvailabilityState(true);
        } else if (!alreadyHandlingUnavailability()) {
            if (playServicesHelper.isStateUserResolvable(serviceState)) {
                MessageWithTitle message =
                        new MessageWithTitle(playServicesHelper.getDialogTitle(serviceState), playServicesHelper.getDialogMessage(serviceState));

                PlayServicesAvailabilityFragmentViewState.AskUserToResolveUnavailabilityState.Builder builder =
                        new PlayServicesAvailabilityFragmentViewState.AskUserToResolveUnavailabilityState.Builder()
                                .setMessageWithTitle(message)
                                .setNegativeButtonTextResId(playServicesHelper.getDialogNegativeButtonText(serviceState))
                                .setPositiveButtonTextResId(playServicesHelper.getDialogPositiveButtonText(serviceState));

                viewStateObservable.setValue(builder.build());
            } else {
                reportPlayservicesAvailabilityState(false);
            }
        }

        currentServiceState = serviceState;
    }

    private boolean alreadyHandlingUnavailability() {
        PlayServicesAvailabilityFragmentViewState currentState = viewStateObservable.getValue();

        return currentState instanceof PlayServicesAvailabilityFragmentViewState.AskUserToResolveUnavailabilityState ||
               currentState instanceof WaitingForUserToResolveUnavailabilityState ||
               currentState instanceof WaitForPlayServicesUpdateState;
    }

    private void reportPlayservicesAvailabilityState(boolean available) {
        viewStateObservable.setValue(new ReportPlayServicesAvailabilityState(available));
    }

    void onPlayServicesAvailabilityStateReported() {
        viewStateObservable.setValue(null);
    }

    void onUserWantsToResolveUnavailabilityState() {
        if (playServicesHelper == null) {
            throw new RuntimeException("You have to call setPlayServicesHelper() before using any other function of this class");
        }

        if (currentServiceState != ServiceState.SERVICE_UPDATING) {
            playServicesHelper.tryToResolveUnavailabilityState(currentServiceState);
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
            }
        }

        scheduleCheckUpdateStateRunnable();
    }

    void onUserDoesNotWantToResolveUnavailabilityState() {
        reportPlayservicesAvailabilityState(false);
    }

    void onGooglePlayServicesAvailable() {
        reportPlayservicesAvailabilityState(true);
    }

    void onGooglePlayServicesUnavailable() {
        if (checkUpdateStateRunnable != null) {
            mainThreadExecutor.cancelDelayed(checkUpdateStateRunnable);

            checkUpdateStateRunnable = null;
        }

        reportPlayservicesAvailabilityState(false);
    }
}
