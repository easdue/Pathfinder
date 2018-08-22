package nl.erikduisters.pathfinder.ui;

import android.app.DownloadManager;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import nl.erikduisters.pathfinder.R;
import nl.erikduisters.pathfinder.data.local.PreferenceManager;
import nl.erikduisters.pathfinder.ui.BaseActivityViewState.DisplayMessageState;
import nl.erikduisters.pathfinder.ui.BaseActivityViewState.DisplayMessageState.DisplayDuration;
import nl.erikduisters.pathfinder.ui.BaseActivityViewState.SetOptionsMenuState;
import nl.erikduisters.pathfinder.ui.BaseActivityViewState.ShowPositiveNegativeDialogState;
import nl.erikduisters.pathfinder.ui.BaseActivityViewState.StartActivityState;
import nl.erikduisters.pathfinder.ui.dialog.MessageWithTitle;
import nl.erikduisters.pathfinder.ui.dialog.PositiveNegativeButtonMessageDialog;
import nl.erikduisters.pathfinder.util.menu.MyMenu;
import nl.erikduisters.pathfinder.util.menu.MyMenuItem;

/**
 * Created by Erik Duisters on 30-07-2018.
 */

public abstract class BaseActivityViewModel extends ViewModel {
    private final MutableLiveData<BaseActivityViewState> baseActivityViewStateObservable;
    private MyMenu optionsMenu;

    protected PreferenceManager preferenceManager;

    protected BaseActivityViewModel(PreferenceManager preferenceManager) {
        this.preferenceManager = preferenceManager;
        baseActivityViewStateObservable = new MutableLiveData<>();

        boolean retryableDownloadsAvailable = preferenceManager.getRetryableMapDownloadIds().size() > 0;

        optionsMenu = new MyMenu();
        optionsMenu.add(new MyMenuItem(R.id.menu_failed_downloads, true, false));
        optionsMenu.add(new MyMenuItem(R.id.menu_retryable_downloads, true, retryableDownloadsAvailable));

        baseActivityViewStateObservable.setValue(new SetOptionsMenuState(optionsMenu));
    }

    LiveData<BaseActivityViewState> getBaseActivityViewStateObservable() { return baseActivityViewStateObservable; };

    void onMapAvailable(String mapName) {

        DisplayMessageState messageState = new DisplayMessageState(DisplayDuration.LONG, R.string.map_available, mapName);

        baseActivityViewStateObservable.setValue(messageState);
    }

    void onMapDownloadFailed(String zipFileName) {
        DisplayMessageState messageState = new DisplayMessageState(DisplayDuration.SHORT, R.string.map_download_failed, zipFileName);

        baseActivityViewStateObservable.setValue(messageState);
    }

    void onFailedMapDownloadsAvailable(int numFailedDownloads) {
        optionsMenu = new MyMenu(optionsMenu);
        optionsMenu.findItem(R.id.menu_failed_downloads).setVisible(numFailedDownloads != 0);

        baseActivityViewStateObservable.setValue(new SetOptionsMenuState(optionsMenu));
    }

    void onRetryableMapDownloadsAvailable(int numRetryableDownloads) {
        optionsMenu = new MyMenu(optionsMenu);
        optionsMenu.findItem(R.id.menu_retryable_downloads).setVisible(numRetryableDownloads != 0);

        baseActivityViewStateObservable.setValue((new SetOptionsMenuState(optionsMenu)));
    }

    void onMapUnzipFailed(String filename) {
        DisplayMessageState messageState = new DisplayMessageState(DisplayDuration.LONG, R.string.map_unzip_failed, filename);

        baseActivityViewStateObservable.setValue(messageState);

        //TODO: Test if onRetryableMapDownloadsAvailable is also called
    }

    void onMessageDisplayed() {
        baseActivityViewStateObservable.setValue(new SetOptionsMenuState(optionsMenu));
    }

    void onOptionsItemSelected(MyMenuItem menuItem) {
        switch (menuItem.getId()) {
            case R.id.menu_failed_downloads:
                StartActivityState startActivityState = new StartActivityState(DownloadManager.ACTION_VIEW_DOWNLOADS);

                baseActivityViewStateObservable.setValue(startActivityState);
                break;
            case R.id.menu_retryable_downloads:
                MessageWithTitle messageWithTitle =
                        new MessageWithTitle(R.string.map_unpack_failed_dialog_title, R.string.map_unpack_failed_dialog_message, preferenceManager.getStorageDir());

                PositiveNegativeButtonMessageDialog.DialogInfo.Builder builder = new PositiveNegativeButtonMessageDialog.DialogInfo.Builder();
                builder.withMessageWithTitle(messageWithTitle)
                        .withShowNeverAskAgain(false)
                        .withPositiveButtonLabelResId(R.string.retry)
                        .withNegativeButtonLabelResId(R.string.open_downloads_app)
                        .withCancellable(true);

                ShowPositiveNegativeDialogState state =
                        new ShowPositiveNegativeDialogState(optionsMenu, builder.build());

                baseActivityViewStateObservable.setValue(state);
        }
    }

    void onActivityStarted() {
        baseActivityViewStateObservable.setValue(new SetOptionsMenuState(optionsMenu));
    }

    void onPositiveButtonClicked(boolean neverAskAgain) {
        preferenceManager.setMapsAreDownloading(true);
        baseActivityViewStateObservable.setValue(new BaseActivityViewState.RetryRetryableMapDownloadsState(optionsMenu));
    }

    void onNegativeButtonClicked(boolean neverAskAgain) {
        preferenceManager.setMapsAreDownloading(true);
        baseActivityViewStateObservable.setValue(new StartActivityState(DownloadManager.ACTION_VIEW_DOWNLOADS));
    }

    void onDialogCancelled() {
        baseActivityViewStateObservable.setValue(new SetOptionsMenuState(optionsMenu));
    }

    void onRetryingRetryableMapDownloads() {
        baseActivityViewStateObservable.setValue(new SetOptionsMenuState(optionsMenu));
    }
}
