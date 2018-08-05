package nl.erikduisters.pathfinder.ui.activity.main_activity;

import android.Manifest;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import javax.inject.Inject;
import javax.inject.Singleton;

import nl.erikduisters.pathfinder.R;
import nl.erikduisters.pathfinder.data.InitDatabaseHelper;
import nl.erikduisters.pathfinder.data.local.GpsManager;
import nl.erikduisters.pathfinder.data.local.PreferenceManager;
import nl.erikduisters.pathfinder.data.usecase.InitDatabase;
import nl.erikduisters.pathfinder.ui.BaseActivityViewModel;
import nl.erikduisters.pathfinder.ui.activity.gps_status.GpsStatusActivity;
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
import nl.erikduisters.pathfinder.ui.activity.settings.SettingsActivity;
import nl.erikduisters.pathfinder.ui.dialog.MessageWithTitle;
import nl.erikduisters.pathfinder.ui.dialog.ProgressDialog;
import nl.erikduisters.pathfinder.ui.fragment.runtime_permission.RuntimePermissionRequest;
import nl.erikduisters.pathfinder.util.DrawableProvider;
import nl.erikduisters.pathfinder.util.StringProvider;
import nl.erikduisters.pathfinder.util.menu.MyMenu;
import nl.erikduisters.pathfinder.util.menu.MyMenuItem;
import timber.log.Timber;

//TODO: Request WRITE_EXTERNAL_STORAGE permission for LeakCanary?
//TODO: Enable/Disable navigation view menu items
//TODO: Add a navigation options menu item allowing the user to manage external maps (eg. delete)
/**
 * Created by Erik Duisters on 03-06-2018.
 */
@Singleton
public class MainActivityViewModel extends BaseActivityViewModel implements InitDatabaseHelper.InitDatabaseListener {
    private MutableLiveData<MainActivityViewState> mainActivityViewStateObservable;
    private MutableLiveData<NavigationViewState> navigationViewStateObservable;
    private MutableLiveData<StartActivityViewState> startActivityViewStateObservable;
    private MutableLiveData<MyMenu> optionsMenuObservable;

    private final GpsManager gpsManager;
    private final InitDatabaseHelper initDatabaseHelper;

    @Inject
    MainActivityViewModel(InitDatabaseHelper initDatabaseHelper, GpsManager gpsManager, PreferenceManager preferenceManager) {
        super(preferenceManager);
        Timber.d("New MainActivityViewModel created");

        mainActivityViewStateObservable = new MutableLiveData<>();
        navigationViewStateObservable = new MutableLiveData<>();
        startActivityViewStateObservable = new MutableLiveData<>();
        optionsMenuObservable = new MutableLiveData<>();

        this.gpsManager = gpsManager;
        this.initDatabaseHelper = initDatabaseHelper;
    }

    LiveData<MainActivityViewState> getMainActivityViewStateObservable() {
        if (mainActivityViewStateObservable.getValue() == null) {
            initDatabase();
        }

        return mainActivityViewStateObservable;
    }

    LiveData<NavigationViewState> getNavigationViewStateObservable() { return navigationViewStateObservable; }
    LiveData<StartActivityViewState> getStartActivityViewStateObservable() { return startActivityViewStateObservable; }
    LiveData<MyMenu> getOptionsMenuObservable() { return optionsMenuObservable; }

    private void initDatabase() {
        ProgressDialog.Properties properties =
                new ProgressDialog.Properties(R.string.initializing_database, true,
                        false, 0, false);

        mainActivityViewStateObservable.setValue(new InitDatabaseState(properties, null));
        initDatabaseHelper.initDatabase(this);
    }

    @Override
    public void onDatabaseInitializationProgress(@NonNull InitDatabase.Progress progress) {
        MainActivityViewState state = mainActivityViewStateObservable.getValue();

        if (state == null || !(state instanceof InitDatabaseState)) {
            throw new IllegalStateException("onDatabaseInitializationProgress() was called but the current state is not InitDatabaseState");
        }

        InitDatabaseState prevInitDatabaseState = (InitDatabaseState) state;

        mainActivityViewStateObservable.setValue(prevInitDatabaseState.createNewWithUpdateProgress(progress));
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
        optionsMenuObservable.setValue(createInitialOptionsMenu());
        navigationViewStateObservable.setValue(createInitialNavigationViewState());
    }

    private NavigationViewState createInitialNavigationViewState() {
        //TODO: Implement UserProfile and load it so the users avatar and username can be shown
        DrawableProvider avatar = new DrawableProvider(R.drawable.vector_drawable_ic_missing_avatar);
        StringProvider user = new StringProvider("");
        MyMenu navigationMenu = createInitialNavigationMenu();

        return new NavigationViewState(avatar, user, navigationMenu);
    }

    private MyMenu createInitialOptionsMenu() {
        MyMenu menu = new MyMenu();

        return menu;
    }

    private MyMenu createInitialNavigationMenu() {
        MyMenu menu = new MyMenu();

        menu.add(new MyMenuItem(R.id.nav_import, true, true));
        menu.add(new MyMenuItem(R.id.nav_login_register, true, true));
        menu.add(new MyMenuItem(R.id.nav_gps_status, true, true));
        menu.add(new MyMenuItem(R.id.nav_settings, true, true));

        return menu;
    }

    void onNavigationMenuItemSelected(MyMenuItem menuItem) {
        switch (menuItem.getId()) {
            case R.id.nav_import:
                //TODO
                break;
            case R.id.nav_login_register:
                //TODO
                break;
            case R.id.nav_gps_status:
                startActivityViewStateObservable
                        .setValue(new StartActivityViewState(GpsStatusActivity.class));
                break;
            case R.id.nav_settings:
                startActivityViewStateObservable
                        .setValue(new StartActivityViewState(SettingsActivity.class));
                break;
        }
    }

    void onActivityStarted() {
        startActivityViewStateObservable.setValue(null);
    }
}
