package nl.erikduisters.pathfinder.ui.activity.main_activity;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;

import javax.inject.Inject;
import javax.inject.Singleton;

import nl.erikduisters.pathfinder.ui.dialog.MessageWithTitle;
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
        mainActivityViewState.setValue(new MainActivityViewState.InitStorageViewState());
    }

    LiveData<MainActivityViewState> getMainActivityViewState() { return mainActivityViewState; }

    void onStorageInitialized() {
        mainActivityViewState.setValue(new MainActivityViewState.InitDatabaseState());
    }

    void handleMessage(@NonNull MessageWithTitle message, boolean isFatal) {
        mainActivityViewState.setValue(new MainActivityViewState.ShowMessageViewState(message, isFatal, mainActivityViewState.getValue()));
    }

    void onMessageDismissed(MainActivityViewState.ShowMessageViewState state) {
        if (state.isFatal) {
            mainActivityViewState.setValue(new MainActivityViewState.FinishState());
        } else {
            mainActivityViewState.setValue(state.prevState);
        }
    }
}
