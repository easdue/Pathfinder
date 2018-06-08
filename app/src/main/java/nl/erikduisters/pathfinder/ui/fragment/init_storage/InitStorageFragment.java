package nl.erikduisters.pathfinder.ui.fragment.init_storage;

import android.os.Bundle;
import android.support.annotation.Nullable;

import nl.erikduisters.pathfinder.ui.BaseFragment;
import nl.erikduisters.pathfinder.ui.dialog.MessageWithTitle;
import nl.erikduisters.pathfinder.ui.dialog.OkMessageDialog;
import nl.erikduisters.pathfinder.ui.dialog.select_storage_dialog.SelectStorageDialog;
import timber.log.Timber;

import static nl.erikduisters.pathfinder.ui.fragment.init_storage.InitStorageFragmentViewState.SelectStorageState;
import static nl.erikduisters.pathfinder.ui.fragment.init_storage.InitStorageFragmentViewState.ShowSpaceRequirementWarningState;
import static nl.erikduisters.pathfinder.ui.fragment.init_storage.InitStorageFragmentViewState.StorageInitializedState;

/**
 * Created by Erik Duisters on 03-06-2018.
 */
public class InitStorageFragment extends BaseFragment<InitStorageFragmentViewModel> implements SelectStorageDialog.OnStorageSelectedListener, OkMessageDialog.OkMessageDialogListener {
    public interface InitStorageFragmentListener {
        void onStorageInitialized();
        void onStorageInitializationFailed(MessageWithTitle message, boolean isFatal);
    }

    private static final String TAG_SELECT_STORAGE_DIALOG = "SelectStorageDialog";
    private static final String TAG_SPACE_REQUIREMENTS_DIALOG = "SpaceRequirementsDialog";

    private InitStorageFragmentListener listener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Timber.e("onCreate(state=%s)", savedInstanceState == null ? "null" : "not null");

        viewModel.getViewState().observe(this, this::render);
    }

    public InitStorageFragment() {}

    public void setListener(InitStorageFragmentListener listener) {
        this.listener = listener;
    }

    @Override
    protected int getLayoutResId() {
        return 0;
    }

    @Override
    protected Class<InitStorageFragmentViewModel> getViewModelClass() {
        return InitStorageFragmentViewModel.class;
    }

    private void render(@Nullable InitStorageFragmentViewState viewState) {
        Timber.e("render(viewState == %s)", viewState == null ? "null" : viewState.getClass().getSimpleName());
        if (viewState == null) {
            return;
        }

        if (viewState instanceof SelectStorageState) {
            showSelectStorageDialog(TAG_SELECT_STORAGE_DIALOG, (SelectStorageState) viewState);
        } else {
            dismissDialogFragment(TAG_SELECT_STORAGE_DIALOG);
        }

        if (viewState instanceof ShowSpaceRequirementWarningState) {
            showOkMessageDialog(TAG_SPACE_REQUIREMENTS_DIALOG,
                    ((ShowSpaceRequirementWarningState) viewState).message);
        } else {
            dismissDialogFragment(TAG_SPACE_REQUIREMENTS_DIALOG);
        }

        if (viewState instanceof StorageInitializedState) {
            if (listener != null) {
                StorageInitializedState state = (StorageInitializedState) viewState;

                if (state.initializationSuccessfull) {
                    listener.onStorageInitialized();
                } else {
                    listener.onStorageInitializationFailed(state.failureMessage, state.isFatal);
                }

                viewModel.storageInitializedStateReported();
            }
        }
    }

    private void showSelectStorageDialog(String tag, SelectStorageState state) {
        SelectStorageDialog dialog = findFragment(tag);

        if (dialog == null) {
            Timber.e("showSelectStorageDialog() - Creating new dialog");
            dialog = SelectStorageDialog.newInstance(state.storageList);
            show(dialog, tag);
        }

        dialog.setOnStorageSelectedListener(this);
    }

    private void showOkMessageDialog(String tag, MessageWithTitle msg) {
        OkMessageDialog dialog = (OkMessageDialog) getFragmentManager()
                .findFragmentByTag(tag);

        if (dialog == null) {
            Timber.e("showOkMessageDialog() - Creating new dialog");
            dialog = OkMessageDialog.newInstance(msg);
            show(dialog, tag);
        }

        dialog.setListener(this);
    }

    @Override
    public void onStorageSelected(Storage selectedStorage) {
        viewModel.onStorageSelected(selectedStorage);
    }

    @Override
    public void onOkMessageDialogDismiss() {
        viewModel.onSpaceRequirementWarningDismissed();
    }
}
