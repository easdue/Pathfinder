package nl.erikduisters.pathfinder.ui.fragment.init_storage;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.os.Environment;
import android.support.annotation.NonNull;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import nl.erikduisters.pathfinder.R;
import nl.erikduisters.pathfinder.data.local.PreferenceManager;
import nl.erikduisters.pathfinder.data.local.StorageHelper;
import nl.erikduisters.pathfinder.ui.dialog.MessageWithTitle;
import nl.erikduisters.pathfinder.ui.dialog.PositiveNegativeButtonMessageDialog;
import nl.erikduisters.pathfinder.ui.fragment.init_storage.InitStorageFragmentViewState.ShowPositiveNegativeButtonMessageDialogState;
import nl.erikduisters.pathfinder.ui.fragment.init_storage.InitStorageFragmentViewState.StorageInitializationFailedState;
import nl.erikduisters.pathfinder.ui.fragment.init_storage.InitStorageFragmentViewState.StorageInitializedState;
import nl.erikduisters.pathfinder.util.FileUtil;
import timber.log.Timber;

/**
 * Created by Erik Duisters on 03-06-2018.
 */
@Singleton
public class InitStorageFragmentViewModel extends ViewModel {
    private final PreferenceManager preferenceManager;
    private final StorageHelper storageHelper;

    private MutableLiveData<InitStorageFragmentViewState> viewState;

    @Inject
    InitStorageFragmentViewModel(PreferenceManager preferenceManager, StorageHelper storageHelper) {
        Timber.e("new InitStorageFragmentViewModel created");
        this.preferenceManager = preferenceManager;
        this.storageHelper = storageHelper;

        viewState = new MutableLiveData<>();
    }

    LiveData<InitStorageFragmentViewState> getViewState() {
        if (viewState.getValue() == null) {
            initStorage();
        }

        return viewState;
    }

    private void initStorage() {
        /*
         * There are phones out there the don't have external storage (eg. Pixel)
         * On API23+ External storage can be adopted to internal storage and getExternalFilesDir() returns only
         * 1 File object for those that report they are emulated through Environemnt.isExternalStorageEmulated(File) (Api21)
         * Environment.getExternalStorageState(File) (Api21) before that getExternalStorageState()
         *
         * On API23 after moving the app to adopted storage context.getFilesDir() points to /mnt/expand/..... but c.getExternalFilesDir()
         * still points to emulated internal storage. More importantly on an emulator at least I can no longer update the app.
         *
         * On API25 app can be updated after moving to external adopted storage but is then moved to internal storage automatically.
         * context.getFilesDir() points to /mnt/expand/.... when app is on adopted storage
         *
         * When app is moved back from adopted to internal storage its data is not moved. Problem is there is no way to detect
         * the presence of adopted storage other then rely on PrefsMan.storageDir(). If the user starts the app while on internal storage
         * new preferences and a new database are created. When the user now moves the app to adopted storage again, the empty database
         * will override whatever was left on adopted storage
         *
         * CONCLUSION: Refuse to work on adopted storage. If app is moved to adopted storage inform user why and ask them to move me back
         *             to internal storage and store data on non adopted sdcard
         *
         * If both internal and external storage are available:
         *      - If internal has more than 2GB available ask if not use external
         *      - If internal or external has < 2GB available warn user
         */

        if (!verifyStorageNotAdopted(storageHelper.getFilesDir())) {
            return;
        }

        String prevStorage = preferenceManager.getStorageDir();

        if (!prevStorage.isEmpty()) {
            File prev = new File(prevStorage);

            if (verifyStorage(prev)) {
                onStorageInitialized();
            }

            return;
        }

        ArrayList<Storage> storageList = buildStorageList();

        if (storageList.size() > 1) {
            InitStorageFragmentViewState selectStorageState = new InitStorageFragmentViewState.SelectStorageState(storageList);

            viewState.setValue(selectStorageState);
        } else {
            onStorageSelected(storageList.get(0));
        }
    }

    private boolean verifyStorage(File storage) {
        boolean isInternal = storageHelper.isInternal(storage);

        boolean allOk;

        allOk = isInternal || verifyStorageIsMounted(storage);
        allOk = allOk && verifyDirectoryStructure(storage);
        allOk = allOk && (isInternal || verifyUUID(storage));

        return allOk;
    }

    private boolean verifyStorageIsMounted(File storage) {
        if (!storageHelper.isStorageMounted(storage)) {
            MessageWithTitle message = new MessageWithTitle(R.string.storage_error, R.string.storage_not_mounted);

            viewState.setValue(new ShowPositiveNegativeButtonMessageDialogState(getDialogInfo(message)));

            return false;
        }

        return true;
    }

    @NonNull
    private PositiveNegativeButtonMessageDialog.DialogInfo getDialogInfo(MessageWithTitle message) {
        PositiveNegativeButtonMessageDialog.DialogInfo.Builder builder = new PositiveNegativeButtonMessageDialog.DialogInfo.Builder();
        builder.withMessageWithTitle(message)
                .withShowNeverAskAgain(false)
                .withPositiveButtonLabelResId(R.string.yes)
                .withNegativeButtonLabelResId(R.string.no)
                .withCancellable(false);

        return builder.build();
    }

    void onPositiveNegativeButtonMessageDialogDismissed(boolean positiveButtonClicked, boolean neverAskAgain) {
        if (positiveButtonClicked) {
            preferenceManager.setStorageDir("");
            preferenceManager.setCacheDir("");
            preferenceManager.setStorageUUID(null);

            initStorage();
        } else {
            viewState.setValue(new StorageInitializationFailedState());
        }
    }

    /*
    void onPositiveNegativeButtonMessageDialogCancelled() {
        //I never allow cancellation
    }
    */

    private boolean verifyDirectoryStructure(File storage) {
        if (!initDirectoryStructure(storage)) {
            MessageWithTitle message = new MessageWithTitle(R.string.storage_error, R.string.storage_initialization_failed);

            viewState.setValue(new ShowPositiveNegativeButtonMessageDialogState(getDialogInfo(message)));

            return false;
        }

        return true;
    }

    private boolean verifyUUID(File storage) {
        UUID uuid = preferenceManager.getStorageUUID();

        if (uuid == null) {
            return verifyUUIdCanBeCreated(storage);
        } else {
            return verifyUUIDExists(storage, uuid);
        }
    }

    private boolean verifyUUIdCanBeCreated(File storage) {
        if (!createUUID(storage)) {
            MessageWithTitle message = new MessageWithTitle(R.string.storage_error, R.string.storage_initialization_failed);

            viewState.setValue(new ShowPositiveNegativeButtonMessageDialogState(getDialogInfo(message)));

            return false;
        }
        return true;
    }

    private boolean verifyUUIDExists(File storage, UUID uuid) {
        File uuidFile = new File(storage, "." + uuid.toString());

        if (!uuidFile.exists()) {
            MessageWithTitle message = new MessageWithTitle(R.string.storage_error, R.string.storage_device_changed);

            viewState.setValue(new ShowPositiveNegativeButtonMessageDialogState(getDialogInfo(message)));

            return false;
        }

        return true;
    }

    private boolean createUUID(File storage) {
        UUID uuid=UUID.randomUUID();

        preferenceManager.setStorageUUID(null);

        boolean success = FileUtil.createFile(new File(storage, "." + uuid.toString()));

        if (success) {
            preferenceManager.setStorageUUID(uuid);
        }

        return success;
    }

    private boolean initDirectoryStructure(File storage) {
        boolean allOk;

        allOk = FileUtil.createDirectory(new File(storage, preferenceManager.getStorageImportSubDir()));
        if (allOk) allOk = FileUtil.createDirectory(new File(storage, preferenceManager.getStorageMapSubDir()));
        if (allOk) allOk = FileUtil.createDirectory(new File(storage, preferenceManager.getStorageCacheSubDir()));
        if (allOk) allOk = FileUtil.createDirectory(new File(storage, preferenceManager.getStorageUserSubDir()));
        if (allOk) allOk = FileUtil.createDirectory(new File(storage, preferenceManager.getStorageRenderThemeSubDir()));
        if (allOk) allOk = FileUtil.createDirectory(new File(storage, Environment.DIRECTORY_DOWNLOADS));
        if (allOk) allOk = FileUtil.createFile(new File(storage, ".nomedia"));

        return allOk;
    }

    private boolean verifyStorageNotAdopted(File storage) {
        if (storageHelper.isStorageAdopted(storage))
        {
            MessageWithTitle message = new MessageWithTitle(R.string.storage_error, R.string.storage_adopted_storage_unsupported);

            viewState.setValue(new InitStorageFragmentViewState.ShowFatalMessageDialogState(message));

            return false;
        }

        return true;
    }

    private ArrayList<Storage> buildStorageList() {
        File internalStorageDir = storageHelper.getFilesDir();
        File internalCacheDir = storageHelper.getCacheDir();

        ArrayList<Storage> storageList = new ArrayList<>();

        storageList.add(new Storage(R.string.storage_internal, 0, internalStorageDir, internalCacheDir));

        File[] externalStorageDirs = storageHelper.getExternalFilesDirs();
        File[] externalCacheDirs = storageHelper.getExternalCacheDirs();

        for (int i = 0, sequenceNr = 1; i < externalStorageDirs.length; i++) {
            File storage = externalStorageDirs[i];

            if (storage == null) continue;

            if (!storageHelper.isStorageEmulated(storage)) {
                storageList.add(new Storage(R.string.storage_external, sequenceNr, storage, externalCacheDirs[i]));
                sequenceNr++;
            }
        }

        return storageList;
    }

    void onStorageSelected(Storage storage) {
        if (verifyStorage(storage.getFileDir())) {
            preferenceManager.setStorageDir(storage.getFileDir().getAbsolutePath());
            preferenceManager.setCacheDir(storage.getCacheDir().getAbsolutePath());

            if (storageHelper.isInternal(storage.getFileDir())) {
                warnUserAboutSpaceRequirements();
                return;
            }

            onStorageInitialized();
        }
    }

    private void warnUserAboutSpaceRequirements() {
        MessageWithTitle message = new MessageWithTitle(R.string.storage_space_warning,
                R.string.storage_space_requirements);
        InitStorageFragmentViewState.ShowSpaceRequirementWarningState showSpaceRequirementWarningState = new InitStorageFragmentViewState.ShowSpaceRequirementWarningState(message);

        viewState.setValue(showSpaceRequirementWarningState);
    }

    void onSpaceRequirementWarningDismissed() {
        onStorageInitialized();
    }

    void onFatalMessageDialogDismissed() {
        viewState.setValue(new StorageInitializationFailedState());
    }

    private void onStorageInitialized() {
        viewState.setValue(new StorageInitializedState());
    }

    void onStorageInitializedStateReported() {
        //As long as the app is not killed assume storage remains initialized
    }

    void onStorageInitializationFailedStateReported() {
        viewState.setValue(null);   //Retry on next run
    }
}
