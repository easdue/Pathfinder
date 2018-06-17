package nl.erikduisters.pathfinder.ui.activity.main_activity;

import android.Manifest;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import javax.inject.Inject;
import javax.inject.Singleton;

import nl.erikduisters.pathfinder.R;
import nl.erikduisters.pathfinder.data.InitDatabaseHelper;
import nl.erikduisters.pathfinder.data.usecase.InitDatabase;
import nl.erikduisters.pathfinder.ui.activity.main_activity.MainActivityViewState.FinishState;
import nl.erikduisters.pathfinder.ui.activity.main_activity.MainActivityViewState.InitDatabaseState;
import nl.erikduisters.pathfinder.ui.activity.main_activity.MainActivityViewState.InitStorageViewState;
import nl.erikduisters.pathfinder.ui.activity.main_activity.MainActivityViewState.RequestRuntimePermissionState;
import nl.erikduisters.pathfinder.ui.activity.main_activity.MainActivityViewState.ShowFatalErrorMessageState;
import nl.erikduisters.pathfinder.ui.activity.main_activity.MainActivityViewState.ShowMessageState;
import nl.erikduisters.pathfinder.ui.dialog.MessageWithTitle;
import nl.erikduisters.pathfinder.ui.fragment.runtime_permission.RuntimePermissionRequest;
import timber.log.Timber;

/**
 * Created by Erik Duisters on 03-06-2018.
 */
@Singleton
public class MainActivityViewModel extends ViewModel implements InitDatabaseHelper.InitDatabaseListener {
    private MutableLiveData<MainActivityViewState> viewStateObservable;

    @Inject
    MainActivityViewModel(InitDatabaseHelper initDatabaseHelper) {
        Timber.d("New MainActivityViewModel created");

        viewStateObservable = new MutableLiveData<>();

        viewStateObservable.setValue(new InitDatabaseState(R.string.initializing_database, null));
        initDatabaseHelper.initDatabase(this);
    }

    LiveData<MainActivityViewState> getViewStateObservable() { return viewStateObservable; }

    @Override
    public void onDatabaseInitializationProgress(@NonNull InitDatabase.Progress progress) {
        viewStateObservable.setValue(new InitDatabaseState(R.string.initializing_database, progress));
    }

    @Override
    public void onDatabaseInitializationComplete() {
        viewStateObservable.setValue(new InitStorageViewState());
    }

    @Override
    public void onDatabaseInitializationError(@NonNull Throwable error) {
        MessageWithTitle message = new MessageWithTitle(R.string.fatal_error, R.string.init_database_failed);
        handleFatalError(message, error);
    }

    void onStorageInitialized() {
        MessageWithTitle rationale =
                new MessageWithTitle(R.string.permission_needed, R.string.location_permission_rational);

        RuntimePermissionRequest request =
                new RuntimePermissionRequest(Manifest.permission.ACCESS_FINE_LOCATION, rationale);

        viewStateObservable.setValue(new RequestRuntimePermissionState(request));
    }

    void handleMessage(@NonNull MessageWithTitle message) {
        viewStateObservable.setValue(new ShowMessageState(message, viewStateObservable.getValue()));
    }

    void onMessageDismissed() {
        ShowMessageState state = (ShowMessageState) viewStateObservable.getValue();

        viewStateObservable.setValue(state.prevState);
    }

    void handleFatalError(@NonNull MessageWithTitle message, @Nullable Throwable throwable) {
        viewStateObservable.setValue(new ShowFatalErrorMessageState(message, throwable));
    }

    void onFatalErrorMessageDismissed() {
        ShowFatalErrorMessageState state = (ShowFatalErrorMessageState) viewStateObservable.getValue();

        if (state.throwable != null) {
            throw new RuntimeException(state.throwable);
        } else {
            viewStateObservable.setValue(new FinishState());
        }
    }

    void onPermissionGranted(String permission) {
        if (permission.equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
            //TODO: Next state
        }
    }

    void onPermissionDenied(@NonNull String permission) {
        if (permission.equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
            MessageWithTitle message = new MessageWithTitle(R.string.fatal_error,
                    R.string.location_permission_is_required);

            handleFatalError(message, null);
        }
    }
}
