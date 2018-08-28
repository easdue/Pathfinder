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

package nl.erikduisters.pathfinder.ui.activity.main_activity;

import android.Manifest;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.io.File;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import nl.erikduisters.pathfinder.R;
import nl.erikduisters.pathfinder.data.InitDatabaseHelper;
import nl.erikduisters.pathfinder.data.local.GpsManager;
import nl.erikduisters.pathfinder.data.local.PreferenceManager;
import nl.erikduisters.pathfinder.data.usecase.InitDatabase;
import nl.erikduisters.pathfinder.service.gpsies_service.SearchTracks;
import nl.erikduisters.pathfinder.service.track_import.GPSiesImportJob;
import nl.erikduisters.pathfinder.service.track_import.ImportJob;
import nl.erikduisters.pathfinder.service.track_import.LocalImportJob;
import nl.erikduisters.pathfinder.ui.BaseActivityViewModel;
import nl.erikduisters.pathfinder.ui.activity.gps_status.GpsStatusActivity;
import nl.erikduisters.pathfinder.ui.activity.main_activity.MainActivityViewState.AskUserToEnableGpsState;
import nl.erikduisters.pathfinder.ui.activity.main_activity.MainActivityViewState.CheckPlayServicesAvailabilityState;
import nl.erikduisters.pathfinder.ui.activity.main_activity.MainActivityViewState.InitDatabaseState;
import nl.erikduisters.pathfinder.ui.activity.main_activity.MainActivityViewState.InitStorageViewState;
import nl.erikduisters.pathfinder.ui.activity.main_activity.MainActivityViewState.InitializedState;
import nl.erikduisters.pathfinder.ui.activity.main_activity.MainActivityViewState.InitializedState.ShowDialogViewState.SelectTracksToImportDialogState;
import nl.erikduisters.pathfinder.ui.activity.main_activity.MainActivityViewState.RequestRuntimePermissionState;
import nl.erikduisters.pathfinder.ui.activity.main_activity.MainActivityViewState.ShowEnableGpsSettingState;
import nl.erikduisters.pathfinder.ui.activity.main_activity.MainActivityViewState.ShowFatalErrorMessageState;
import nl.erikduisters.pathfinder.ui.activity.main_activity.MainActivityViewState.StartActivityViewState;
import nl.erikduisters.pathfinder.ui.activity.main_activity.MainActivityViewState.WaitingForGpsToBeEnabledState;
import nl.erikduisters.pathfinder.ui.activity.settings.SettingsActivity;
import nl.erikduisters.pathfinder.ui.dialog.MessageWithTitle;
import nl.erikduisters.pathfinder.ui.dialog.PositiveNegativeButtonMessageDialog;
import nl.erikduisters.pathfinder.ui.dialog.ProgressDialog;
import nl.erikduisters.pathfinder.ui.fragment.runtime_permission.RuntimePermissionRequest;
import nl.erikduisters.pathfinder.util.DrawableProvider;
import nl.erikduisters.pathfinder.util.StringProvider;
import nl.erikduisters.pathfinder.util.menu.MyMenu;
import nl.erikduisters.pathfinder.util.menu.MyMenuItem;
import timber.log.Timber;

//TODO: GPLv3 notices in every file
//TODO: Request WRITE_EXTERNAL_STORAGE permission for LeakCanary?
//TODO: Enable/Disable navigation view menu items
//TODO: Add a navigation options menu item allowing the user to manage external maps (eg. delete/download)
//TODO: Allow the user to move storage from internal to external or visa versa
//TODO: Allow the user to start and stop recording a track
//TODO: Better about dialog
/**
 * Created by Erik Duisters on 03-06-2018.
 */
@Singleton
public class MainActivityViewModel extends BaseActivityViewModel implements InitDatabaseHelper.InitDatabaseListener {
    private MutableLiveData<MainActivityViewState> mainActivityViewStateObservable;
    private MutableLiveData<StartActivityViewState> startActivityViewStateObservable;

    private final GpsManager gpsManager;
    private final InitDatabaseHelper initDatabaseHelper;
    private InitializedState.ShowDialogViewState restoredShowDialogViewState;

    @Inject
    MainActivityViewModel(InitDatabaseHelper initDatabaseHelper, GpsManager gpsManager, PreferenceManager preferenceManager) {
        super(preferenceManager);
        Timber.d("New MainActivityViewModel created");

        mainActivityViewStateObservable = new MutableLiveData<>();
        startActivityViewStateObservable = new MutableLiveData<>();

        this.gpsManager = gpsManager;
        this.initDatabaseHelper = initDatabaseHelper;
    }

    LiveData<MainActivityViewState> getMainActivityViewStateObservable() {
        //TODO: Always check storage, runtimePermissions, etc ...?
        if (mainActivityViewStateObservable.getValue() == null) {
            initDatabase();
        }

        return mainActivityViewStateObservable;
    }
    LiveData<StartActivityViewState> getStartActivityViewStateObservable() { return startActivityViewStateObservable; }

    //TODO: Database is now stored on internal storage. It would be better to store it in the selected storage location so first initStorage and then init database
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

        mainActivityViewStateObservable.setValue(new ShowFatalErrorMessageState(message, true, error));
    }

    void onStorageInitialized() {
        MessageWithTitle rationale =
                new MessageWithTitle(R.string.permission_needed, R.string.location_permission_rational);

        RuntimePermissionRequest request =
                new RuntimePermissionRequest(Manifest.permission.ACCESS_FINE_LOCATION, rationale);

        mainActivityViewStateObservable.setValue(new RequestRuntimePermissionState(request));
    }

    void onStorageInitializationFailed() {
        //Do nothing, next time app resumes/restarts without being killed InitStorageViewState is still the current viewState
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

            mainActivityViewStateObservable.setValue(new ShowFatalErrorMessageState(message, false, null,
                    mainActivityViewStateObservable.getValue()));
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

            PositiveNegativeButtonMessageDialog.DialogInfo.Builder builder = new PositiveNegativeButtonMessageDialog.DialogInfo.Builder();
            builder.withMessageWithTitle(message)
                    .withShowNeverAskAgain(true)
                    .withPositiveButtonLabelResId(R.string.yes)
                    .withNegativeButtonLabelResId(R.string.no)
                    .withCancellable(true);

            mainActivityViewStateObservable.setValue(new AskUserToEnableGpsState(builder.build()));
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
        mainActivityViewStateObservable.setValue(new InitializedState(createInitialOptionsMenu(), createInitialNavigationViewState(), restoredShowDialogViewState));
        restoredShowDialogViewState = null;
    }

    private InitializedState.NavigationViewState createInitialNavigationViewState() {
        //TODO: Implement UserProfile and load it so the users avatar and username can be shown
        DrawableProvider avatar = new DrawableProvider(R.drawable.vector_drawable_ic_missing_avatar);
        StringProvider user = new StringProvider("");
        MyMenu navigationMenu = createInitialNavigationMenu();

        return new InitializedState.NavigationViewState(avatar, user, navigationMenu);
    }

    private MyMenu createInitialOptionsMenu() {
        MyMenu menu = new MyMenu();

        return menu;
    }

    private MyMenu createInitialNavigationMenu() {
        MyMenu menu = new MyMenu();

        menu.add(new MyMenuItem(R.id.nav_import, true, true));
        menu.add(new MyMenuItem(R.id.nav_login_register, false, true));
        menu.add(new MyMenuItem(R.id.nav_gps_status, true, true));
        menu.add(new MyMenuItem(R.id.nav_settings, true, true));
        menu.add(new MyMenuItem(R.id.nav_about, true, true));

        return menu;
    }

    private InitializedState getCurrentInitializedState() {
        MainActivityViewState currentState = mainActivityViewStateObservable.getValue();

        if (!(currentState instanceof InitializedState)) {
            throw new IllegalStateException("Expecting current MainActivityViewState to be Initialized state but it is Initialized");
        }

        return (InitializedState) currentState;
    }

    void onNavigationMenuItemSelected(MyMenuItem menuItem) {
        switch (menuItem.getId()) {
            case R.id.nav_import: {
                InitializedState currentState = getCurrentInitializedState();

                mainActivityViewStateObservable.setValue(
                        new InitializedState(currentState.optionsMenu, currentState.navigationViewState,
                                new InitializedState.ShowDialogViewState.ShowImportSettingsDialogState()));
                break;
            }
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
            case R.id.nav_about: {
                InitializedState currentState = getCurrentInitializedState();

                MessageWithTitle messageWithTitle = new MessageWithTitle(R.string.about_dialog_title, R.string.about_dialog_message);
                mainActivityViewStateObservable.setValue(
                        new InitializedState(currentState.optionsMenu, currentState.navigationViewState,
                                new InitializedState.ShowDialogViewState.ShowOkMessageDialogState(messageWithTitle)));
                break;
            }
        }
    }

    void onActivityStarted() {
        startActivityViewStateObservable.setValue(null);
    }

    void onFatalErrorMessageDismissed() {
        ShowFatalErrorMessageState showFatalErrorMessagestate = (ShowFatalErrorMessageState) mainActivityViewStateObservable.getValue();

        if (showFatalErrorMessagestate == null) {
            throw new IllegalStateException("onFatalErrorMessageDismissed called but current state is not ShowFatalErrorMessageState");
        }

        if (showFatalErrorMessagestate.throwable != null) {
            throw new RuntimeException(showFatalErrorMessagestate.throwable);
        } else {
            if (showFatalErrorMessagestate.nextState != null) {
                mainActivityViewStateObservable.setValue(showFatalErrorMessagestate.nextState);
            }
        }
    }

    void onImportSettingsDialogDismissed(SearchTracks.JobInfo jobInfo) {
        InitializedState currentState = getCurrentInitializedState();

        mainActivityViewStateObservable.setValue(new InitializedState(currentState.optionsMenu,
                currentState.navigationViewState, new SelectTracksToImportDialogState(jobInfo)));
    }

    void onImportSettingsDialogDismissed(List<File> filesToImport, TrackImportScheduler trackImportScheduler) {
        ImportJob.JobInfo jobInfo = new LocalImportJob.JobInfo(filesToImport);
        trackImportScheduler.scheduleTrackImport(jobInfo);
    }

    void onImportSettingsDialogCancelled() {
        InitializedState currentState = getCurrentInitializedState();

        mainActivityViewStateObservable.setValue(new InitializedState(currentState.optionsMenu, currentState.navigationViewState));
    }

    //TODO: rename
    void onSelectTracksToImportDialogDismissed(List<String> selectedTrackFileIds, TrackImportScheduler trackImportScheduler) {
        ImportJob.JobInfo jobInfo = new GPSiesImportJob.JobInfo(selectedTrackFileIds);
        trackImportScheduler.scheduleTrackImport(jobInfo);

        InitializedState currentState = getCurrentInitializedState();
        mainActivityViewStateObservable.setValue(new InitializedState(currentState.optionsMenu, currentState.navigationViewState));
    }

    void onSelectTracksToImportDialogCancelled() {
        InitializedState currentState = getCurrentInitializedState();

        mainActivityViewStateObservable.setValue(new InitializedState(currentState.optionsMenu, currentState.navigationViewState));
    }

    void onOkMessageDialogDismissed() {
        InitializedState currentState = getCurrentInitializedState();

        mainActivityViewStateObservable.setValue(new InitializedState(currentState.optionsMenu, currentState.navigationViewState));
    }

    Parcelable onSaveInstanceState() {
        return new SavedState(mainActivityViewStateObservable.getValue());
    }

    /**
     * Must be called before starting to observe any LiveData!
     */
    void onRestoreInstanceState(Parcelable savedState) {
        if (savedState == null || mainActivityViewStateObservable.getValue() != null) {
            Timber.d("Not restoring state");
        }

        SavedState state = (SavedState) savedState;

        if (state.initialized && state.showDialogViewState != null) {
            restoredShowDialogViewState = state.showDialogViewState;
        }
    }

    private static class SavedState implements Parcelable {
        boolean initialized;
        InitializedState.ShowDialogViewState showDialogViewState;

        public SavedState(MainActivityViewState currentViewState) {
            if (currentViewState instanceof InitializedState) {
                initialized = true;
                showDialogViewState = ((InitializedState) currentViewState).showDialogViewState;
            } else {
                initialized = false;
                showDialogViewState = null;
            }
        }

        protected SavedState(Parcel in) {
            this.initialized = in.readByte() != 0;
            this.showDialogViewState = in.readParcelable(InitializedState.ShowDialogViewState.class.getClassLoader());
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeByte(this.initialized ? (byte) 1 : (byte) 0);
            dest.writeParcelable(this.showDialogViewState, flags);
        }

        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel source) {
                return new SavedState(source);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}
