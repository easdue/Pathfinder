package nl.erikduisters.pathfinder.ui.fragment.play_services;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.google.android.gms.common.GoogleApiAvailability;

import nl.erikduisters.pathfinder.ui.BaseFragment;
import nl.erikduisters.pathfinder.ui.RequestCode;
import nl.erikduisters.pathfinder.ui.dialog.PositiveNegativeButtonMessageDialog;
import nl.erikduisters.pathfinder.ui.dialog.ProgressDialog;
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
        implements PlayServicesHelper, PositiveNegativeButtonMessageDialog.Listener {

    public interface PlayServicesAvailabilityFragmentListener {
        void onPlayServicesAvailable();
        void onPlayServicesUnavailable();
    }

    private static final String KEY_ASK_USER_TO_RESOLVE_UNAVAILABILITY_DIALOG = "AskUserToResolveUnavailabilityDialog";
    private static final String KEY_WAITING_FOR_PLAY_SERVICES_UPDATE_DIALOG = "WaitingForPlayServicesUpdateDialog";

    private @Nullable PlayServicesAvailabilityFragmentListener listener;

    public PlayServicesFragment() {}

    public static PlayServicesFragment newInstance() {
        return new PlayServicesFragment();
    }

    public void setListener(@Nullable PlayServicesAvailabilityFragmentListener listener) {
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
        viewModel.getViewStateObservable().observe(this, this::render);
    }

    @Override
    public void onResume() {
        super.onResume();

        viewModel.checkPlayServicesAvailability();
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
    }

    private void ShowAskUserToResolveUnavailabilityDialog(AskUserToResolveUnavailabilityState viewState, String tag) {
        PositiveNegativeButtonMessageDialog dialog = findFragment(tag);

        if (dialog == null) {
            dialog = PositiveNegativeButtonMessageDialog.newInstance(viewState.messageWithTitle,
                    viewState.positiveButtonTextResId, viewState.negativeButtonTextResId);
            dialog.setCancelable(false);

            show(dialog, tag);
        }

        dialog.setListener(this);
    }

    private void ShowProgressDialog(WaitForPlayServicesUpdateState state, String tag) {
        ProgressDialog dialog = findFragment(tag);

        if (dialog == null) {
            dialog = ProgressDialog.newInstance(state.dialogProperties);

            show(dialog, tag);
        }

        dialog.setKeyProgressMessage(state.progressMessageResId);

        dialog.setListener(() -> viewModel.onGooglePlayServicesUnavailable());
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
    public void tryToResolveUnavailabilityState(@ServiceState int state) {
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
        Timber.d("onActivityResult");
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode) {
            case RequestCode.GOOGLEPLAY_ERROR_RESOLUTION_REQUEST:
                //resultCode is always RESULT_CANCELED.
                if (getGooglePlayServicesState() == ServiceState.SERVICE_OK) {
                    viewModel.onGooglePlayServicesAvailable();
                } else {
                    viewModel.onGooglePlayServicesUnavailable();
                }
        }
    }

    @Override
    public void onPositiveButtonClicked() {
        viewModel.onUserWantsToResolveUnavailabilityState();
    }

    @Override
    public void onNegativeButtonClicked() {
        viewModel.onUserDoesNotWantToResolveUnavailabilityState();
    }
}
