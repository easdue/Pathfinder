package nl.erikduisters.pathfinder.ui.activity.main_activity;

import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import nl.erikduisters.pathfinder.data.usecase.InitDatabase;
import nl.erikduisters.pathfinder.service.gpsies_service.SearchTracks;
import nl.erikduisters.pathfinder.ui.dialog.MessageWithTitle;
import nl.erikduisters.pathfinder.ui.dialog.PositiveNegativeButtonMessageDialog;
import nl.erikduisters.pathfinder.ui.dialog.ProgressDialog;
import nl.erikduisters.pathfinder.ui.fragment.runtime_permission.RuntimePermissionRequest;
import nl.erikduisters.pathfinder.util.DrawableProvider;
import nl.erikduisters.pathfinder.util.StringProvider;
import nl.erikduisters.pathfinder.util.menu.MyMenu;

/**
 * Created by Erik Duisters on 03-06-2018.
 */
interface MainActivityViewState {
    class InitDatabaseState implements MainActivityViewState {
        @NonNull final ProgressDialog.Properties dialogProperties;
        @Nullable final InitDatabase.Progress progress;

        InitDatabaseState(@NonNull ProgressDialog.Properties dialogProperties, @Nullable InitDatabase.Progress progress) {
            this.dialogProperties = dialogProperties;
            this.progress = progress;
        }

        InitDatabaseState createNewWithUpdateProgress(@NonNull InitDatabase.Progress progress) {
            return new InitDatabaseState(this.dialogProperties, progress);
        }
    }

    class InitStorageViewState implements MainActivityViewState {}

    class RequestRuntimePermissionState implements MainActivityViewState {
        final RuntimePermissionRequest request;

        RequestRuntimePermissionState(RuntimePermissionRequest request) {
            this.request = request;
        }
    }

    class CheckPlayServicesAvailabilityState implements MainActivityViewState {
    }

    class ShowFatalErrorMessageState implements MainActivityViewState {
        @NonNull final MessageWithTitle message;
        final boolean finishOnDismiss;
        /**
         * For ViewModel internal use
         */
        @Nullable final Throwable throwable;
        /**
         * For ViewModel internal use
         */

        @Nullable final MainActivityViewState nextState;

        ShowFatalErrorMessageState(@NonNull MessageWithTitle message, boolean finishOnDismiss, @Nullable Throwable throwable) {
            this(message, finishOnDismiss, throwable, null);
        }

        public ShowFatalErrorMessageState(@NonNull MessageWithTitle message, boolean finishOnDismiss, @NonNull Throwable throwable, MainActivityViewState nextState) {
            this.message = message;
            this.finishOnDismiss = finishOnDismiss;
            this.throwable = throwable;
            this.nextState = nextState;
        }
    }

    class AskUserToEnableGpsState implements MainActivityViewState {
        @NonNull PositiveNegativeButtonMessageDialog.DialogInfo dialogInfo;

        AskUserToEnableGpsState(@NonNull PositiveNegativeButtonMessageDialog.DialogInfo dialogInfo) {
            this.dialogInfo = dialogInfo;
        }
    }

    class ShowEnableGpsSettingState implements MainActivityViewState {}
    class WaitingForGpsToBeEnabledState implements MainActivityViewState {}

    class InitializedState implements MainActivityViewState {
        @NonNull final MyMenu optionsMenu;
        @NonNull final NavigationViewState navigationViewState;
        @Nullable final ShowDialogViewState showDialogViewState;

        InitializedState(@NonNull MyMenu optionsMenu, @NonNull NavigationViewState navigationViewState) {
            this(optionsMenu, navigationViewState, null);
        }

        InitializedState(@NonNull MyMenu optionsMenu, @NonNull NavigationViewState navigationViewState, @Nullable ShowDialogViewState showDialogViewState) {
            this.optionsMenu = optionsMenu;
            this.navigationViewState = navigationViewState;
            this.showDialogViewState = showDialogViewState;
        }

        static class NavigationViewState {
            @NonNull final DrawableProvider avatar;
            @NonNull final StringProvider userName;
            @NonNull final MyMenu navigationMenu;

            NavigationViewState(@NonNull DrawableProvider avatar,
                                @NonNull StringProvider userName,
                                @NonNull MyMenu navigationMenu) {
                this.avatar = avatar;
                this.userName = userName;
                this.navigationMenu = navigationMenu;
            }
        }

        interface ShowDialogViewState extends Parcelable {
            final class ShowImportSettingsDialogState implements ShowDialogViewState {
                public ShowImportSettingsDialogState() {
                }

                protected ShowImportSettingsDialogState(Parcel in) {
                }

                @Override
                public int describeContents() {
                    return 0;
                }

                @Override
                public void writeToParcel(Parcel dest, int flags) {
                }

                public static final Parcelable.Creator<ShowImportSettingsDialogState> CREATOR = new Parcelable.Creator<ShowImportSettingsDialogState>() {
                    @Override
                    public ShowImportSettingsDialogState createFromParcel(Parcel source) {
                        return new ShowImportSettingsDialogState(source);
                    }

                    @Override
                    public ShowImportSettingsDialogState[] newArray(int size) {
                        return new ShowImportSettingsDialogState[size];
                    }
                };
            }

            final class SelectTracksToImportDialogState implements ShowDialogViewState {
                @NonNull final SearchTracks.JobInfo jobInfo;

                public SelectTracksToImportDialogState(@NonNull SearchTracks.JobInfo jobInfo) {
                    this.jobInfo = jobInfo;
                }

                @Override
                public int describeContents() {
                    return 0;
                }

                @Override
                public void writeToParcel(Parcel dest, int flags) {
                    dest.writeParcelable(this.jobInfo, flags);
                }

                protected SelectTracksToImportDialogState(Parcel in) {
                    this.jobInfo = in.readParcelable(SearchTracks.JobInfo.class.getClassLoader());
                }

                public static final Creator<SelectTracksToImportDialogState> CREATOR = new Creator<SelectTracksToImportDialogState>() {
                    @Override
                    public SelectTracksToImportDialogState createFromParcel(Parcel source) {
                        return new SelectTracksToImportDialogState(source);
                    }

                    @Override
                    public SelectTracksToImportDialogState[] newArray(int size) {
                        return new SelectTracksToImportDialogState[size];
                    }
                };
            }
        }
    }

    class StartActivityViewState {
        @NonNull private final Class<?> activityClass;

        StartActivityViewState(@NonNull Class activityClass) {
            this.activityClass = activityClass;
        }

        Intent getIntent(Context context) {
            return new Intent(context, activityClass);
        }
    }
}
