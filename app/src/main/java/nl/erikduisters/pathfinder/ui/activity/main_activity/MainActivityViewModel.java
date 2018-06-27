package nl.erikduisters.pathfinder.ui.activity.main_activity;

import android.Manifest;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import nl.erikduisters.pathfinder.R;
import nl.erikduisters.pathfinder.data.InitDatabaseHelper;
import nl.erikduisters.pathfinder.data.local.GpsManager;
import nl.erikduisters.pathfinder.data.local.PreferenceManager;
import nl.erikduisters.pathfinder.data.usecase.InitDatabase;
import nl.erikduisters.pathfinder.ui.MyMenuItem;
import nl.erikduisters.pathfinder.ui.activity.main_activity.MainActivityViewState.AskUserToEnableGpsState;
import nl.erikduisters.pathfinder.ui.activity.main_activity.MainActivityViewState.CheckPlayServicesAvailabilityState;
import nl.erikduisters.pathfinder.ui.activity.main_activity.MainActivityViewState.FinishState;
import nl.erikduisters.pathfinder.ui.activity.main_activity.MainActivityViewState.InitDatabaseState;
import nl.erikduisters.pathfinder.ui.activity.main_activity.MainActivityViewState.InitStorageViewState;
import nl.erikduisters.pathfinder.ui.activity.main_activity.MainActivityViewState.InitializedState;
import nl.erikduisters.pathfinder.ui.activity.main_activity.MainActivityViewState.RequestRuntimePermissionState;
import nl.erikduisters.pathfinder.ui.activity.main_activity.MainActivityViewState.ShowEnableGpsSettingState;
import nl.erikduisters.pathfinder.ui.activity.main_activity.MainActivityViewState.ShowFatalErrorMessageState;
import nl.erikduisters.pathfinder.ui.activity.main_activity.MainActivityViewState.ShowMessageState;
import nl.erikduisters.pathfinder.ui.activity.main_activity.MainActivityViewState.WaitingForGpsToBeEnabledState;
import nl.erikduisters.pathfinder.ui.dialog.MessageWithTitle;
import nl.erikduisters.pathfinder.ui.dialog.ProgressDialog;
import nl.erikduisters.pathfinder.ui.fragment.runtime_permission.RuntimePermissionRequest;
import nl.erikduisters.pathfinder.util.DrawableType;
import nl.erikduisters.pathfinder.util.StringType;
import timber.log.Timber;

//TODO: Request WRITE_EXTERNAL_STORAGE permission for LeakCanary?
/**
 * Created by Erik Duisters on 03-06-2018.
 */
@Singleton
public class MainActivityViewModel extends ViewModel implements InitDatabaseHelper.InitDatabaseListener {
    private MutableLiveData<MainActivityViewState> mainActivityViewStateObservable;
    private MutableLiveData<NavigationViewState> navigationViewStateObservable;
    private final GpsManager gpsManager;
    private final PreferenceManager preferenceManager;

    @Inject
    MainActivityViewModel(InitDatabaseHelper initDatabaseHelper, GpsManager gpsManager, PreferenceManager preferenceManager) {
        Timber.d("New MainActivityViewModel created");

        mainActivityViewStateObservable = new MutableLiveData<>();
        navigationViewStateObservable = new MutableLiveData<>();

        this.gpsManager = gpsManager;
        this.preferenceManager = preferenceManager;

        ProgressDialog.Properties properties =
                new ProgressDialog.Properties(R.string.initializing_database, true,
                        false, 0, false);

        mainActivityViewStateObservable.setValue(new InitDatabaseState(properties, null));
        initDatabaseHelper.initDatabase(this);
    }

    LiveData<MainActivityViewState> getMainActivityViewStateObservable() { return mainActivityViewStateObservable; }
    LiveData<NavigationViewState> getNavigationViewStateObservable() { return navigationViewStateObservable; }

    @Override
    public void onDatabaseInitializationProgress(@NonNull InitDatabase.Progress progress) {
        MainActivityViewState state = mainActivityViewStateObservable.getValue();

        if (state == null || !(state instanceof InitDatabaseState)) {
            throw new IllegalStateException("onDatabaseInitializationProgress() was called but the current state is not InitDatabaseState");
        }

        InitDatabaseState prevInitDatabaseState = (InitDatabaseState) state;

        mainActivityViewStateObservable.setValue(prevInitDatabaseState.updateProgress(progress));
    }

    @Override
    public void onDatabaseInitializationComplete() {
        mainActivityViewStateObservable.setValue(new InitStorageViewState());
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

        mainActivityViewStateObservable.setValue(new RequestRuntimePermissionState(request));
    }

    void handleMessage(@NonNull MessageWithTitle message) {
        mainActivityViewStateObservable.setValue(new ShowMessageState(message, mainActivityViewStateObservable.getValue()));
    }

    void onMessageDismissed() {
        ShowMessageState state = (ShowMessageState) mainActivityViewStateObservable.getValue();

        mainActivityViewStateObservable.setValue(state.prevState);
    }

    void handleFatalError(@NonNull MessageWithTitle message, @Nullable Throwable throwable) {
        mainActivityViewStateObservable.setValue(new ShowFatalErrorMessageState(message, throwable));
    }

    void onFatalErrorMessageDismissed() {
        ShowFatalErrorMessageState state = (ShowFatalErrorMessageState) mainActivityViewStateObservable.getValue();

        if (state.throwable != null) {
            throw new RuntimeException(state.throwable);
        } else {
            mainActivityViewStateObservable.setValue(new FinishState());
        }
    }

    void onPermissionGranted(String permission) {
        if (permission.equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
            gpsManager.onAccessFineLocationPermitted();
            mainActivityViewStateObservable.setValue(new CheckPlayServicesAvailabilityState());
        }
    }

    void onPermissionDenied(@NonNull String permission) {
        if (permission.equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
            MessageWithTitle message = new MessageWithTitle(R.string.fatal_error,
                    R.string.location_permission_is_required);

            handleFatalError(message, null);
        }
    }

    void onPlayServicesAvailable() {
        gpsManager.onGooglePlayServicesAvailable();

        setInitializedState();
    }

    void onPlayServicesUnavailable() {
        boolean askToEnableGps = preferenceManager.askToEnableGps();

        if (askToEnableGps && gpsManager.hasGps() && !gpsManager.isGpsEnabled()) {
            MessageWithTitle message = new MessageWithTitle(R.string.enable_gps, R.string.enable_gps_message);
            mainActivityViewStateObservable.setValue(new AskUserToEnableGpsState(message, true, R.string.yes, R.string.no));
        } else {
            //TODO: Show a snackbar informing the user that the gps is not enabled
            setInitializedState();
        }
    }

    void onUserWantsToEnableGps(boolean neverAskAgain) {
        handleNeverAskAgain(neverAskAgain);
        mainActivityViewStateObservable.setValue(new ShowEnableGpsSettingState());
    }

    void onUserDoesNotWantToEnableGps(boolean neverAskAgain) {
        handleNeverAskAgain(neverAskAgain);
        setInitializedState();
    }

    private void handleNeverAskAgain(boolean neverAskAgain) {
        if (neverAskAgain) {
            preferenceManager.setAskToEnableGps(false);
        }
    }

    void onWaitingForGpsToBeEnabled() {
        mainActivityViewStateObservable.setValue(new WaitingForGpsToBeEnabledState());
    }

    void onFinishedWaitingForGpsToBeEnabled() {
        setInitializedState();
    }

    private void setInitializedState() {
        mainActivityViewStateObservable.setValue(new InitializedState());
        navigationViewStateObservable.setValue(createInitialNavigationViewState());
    }

    private NavigationViewState createInitialNavigationViewState() {
        //TODO: Implement UserProfile and load it so the users avatar and username can be shown
        DrawableType avatar = new DrawableType(R.drawable.vector_drawable_ic_missing_avatar);
        StringType user = new StringType("");
        List<MyMenuItem> navigationMenu = createInitialNavigationMenu();

        return new NavigationViewState(avatar, user, navigationMenu);
    }

    private List<MyMenuItem> createInitialNavigationMenu() {
        List<MyMenuItem> menu = new ArrayList<>();
        menu.add(new MyMenuItem(R.id.nav_import, true, true));
        menu.add(new MyMenuItem(R.id.nav_login_register, true, true));
        menu.add(new MyMenuItem(R.id.nav_gps_status, true, true));
        menu.add(new MyMenuItem(R.id.nav_settings, true, true));

        return menu;
    }
}
