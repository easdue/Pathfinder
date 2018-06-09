package nl.erikduisters.pathfinder.ui.activity.main_activity;

import android.Manifest;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;

import javax.inject.Inject;
import javax.inject.Singleton;

import nl.erikduisters.pathfinder.R;
import nl.erikduisters.pathfinder.ui.activity.main_activity.MainActivityViewState.InitStorageViewState;
import nl.erikduisters.pathfinder.ui.activity.main_activity.MainActivityViewState.RequestRuntimePermissionState;
import nl.erikduisters.pathfinder.ui.dialog.MessageWithTitle;
import nl.erikduisters.pathfinder.ui.fragment.runtime_permission.RuntimePermissionRequest;
import timber.log.Timber;

/**
 * Created by Erik Duisters on 03-06-2018.
 */
@Singleton
public class MainActivityViewModel extends ViewModel {
    private MutableLiveData<MainActivityViewState> mainActivityViewState;

    @Inject
    MainActivityViewModel() {
        Timber.d("New MainActivityViewModel created");

        mainActivityViewState = new MutableLiveData<>();
        mainActivityViewState.setValue(new InitStorageViewState());
    }

    LiveData<MainActivityViewState> getMainActivityViewState() { return mainActivityViewState; }

    void onStorageInitialized() {
        MessageWithTitle rationale =
                new MessageWithTitle(R.string.permission_needed, R.string.location_permission_rational);

        RuntimePermissionRequest request =
                new RuntimePermissionRequest(Manifest.permission.ACCESS_FINE_LOCATION, rationale);

        mainActivityViewState.setValue(new RequestRuntimePermissionState(request));
    }

    void handleMessage(@NonNull MessageWithTitle message, boolean isFatal) {
        mainActivityViewState.setValue(new MainActivityViewState.ShowMessageState(message, isFatal, mainActivityViewState.getValue()));
    }

    void onMessageDismissed(MainActivityViewState.ShowMessageState state) {
        if (state.isFatal) {
            mainActivityViewState.setValue(new MainActivityViewState.FinishState());
        } else {
            mainActivityViewState.setValue(state.prevState);
        }
    }

    void onPermissionGranted(String permission) {
        //TODO: Init database

    }

    void onPermissionDenied(@NonNull String permission) {
        if (permission.equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
            MessageWithTitle message = new MessageWithTitle(R.string.fatal_error,
                    R.string.location_permission_is_required);

            mainActivityViewState.setValue(new MainActivityViewState.ShowMessageState(message, true));
        }
    }
}
