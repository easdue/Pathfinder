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

package nl.erikduisters.pathfinder.ui.fragment.init_storage;

import android.os.Bundle;
import android.support.annotation.Nullable;

import nl.erikduisters.pathfinder.ui.BaseFragment;
import nl.erikduisters.pathfinder.ui.dialog.FatalMessageDialog;
import nl.erikduisters.pathfinder.ui.dialog.MessageWithTitle;
import nl.erikduisters.pathfinder.ui.dialog.OkMessageDialog;
import nl.erikduisters.pathfinder.ui.dialog.PositiveNegativeButtonMessageDialog;
import nl.erikduisters.pathfinder.ui.dialog.select_storage_dialog.SelectStorageDialog;
import nl.erikduisters.pathfinder.ui.fragment.HeadlessFragment;
import nl.erikduisters.pathfinder.ui.fragment.init_storage.InitStorageFragmentViewState.ShowFatalMessageDialogState;
import nl.erikduisters.pathfinder.ui.fragment.init_storage.InitStorageFragmentViewState.ShowPositiveNegativeButtonMessageDialogState;
import timber.log.Timber;

import static nl.erikduisters.pathfinder.ui.fragment.init_storage.InitStorageFragmentViewState.SelectStorageState;
import static nl.erikduisters.pathfinder.ui.fragment.init_storage.InitStorageFragmentViewState.ShowSpaceRequirementWarningState;
import static nl.erikduisters.pathfinder.ui.fragment.init_storage.InitStorageFragmentViewState.StorageInitializedState;

/**
 * Created by Erik Duisters on 03-06-2018.
 */
public class InitStorageFragment
        extends BaseFragment<InitStorageFragmentViewModel>
        implements SelectStorageDialog.OnStorageSelectedListener, OkMessageDialog.OkMessageDialogListener,
                   HeadlessFragment {
    public interface InitStorageFragmentListener {
        void onStorageInitialized();
        void onStorageInitializationFailed();
    }

    private static final String TAG_SELECT_STORAGE_DIALOG = "SelectStorageDialog";
    private static final String TAG_SPACE_REQUIREMENTS_DIALOG = "SpaceRequirementsDialog";

    private static final String TAG_POSITIVE_NEGATIVE_BUTTON_MESSAGE_DIALOG = "PositiveNegativeButtonMessageDialog";
    private static final String TAG_FATAL_MESSAGE_DIALOG = "FatalMessageDialog";

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

        if (viewState instanceof ShowPositiveNegativeButtonMessageDialogState) {
            showPositiveNegativeButtonMessageDialog((ShowPositiveNegativeButtonMessageDialogState) viewState, TAG_POSITIVE_NEGATIVE_BUTTON_MESSAGE_DIALOG);
        } else {
            dismissDialogFragment(TAG_POSITIVE_NEGATIVE_BUTTON_MESSAGE_DIALOG);
        }

        if (viewState instanceof ShowFatalMessageDialogState) {
            showFatalMessageDialogState((ShowFatalMessageDialogState) viewState, TAG_FATAL_MESSAGE_DIALOG);
        } else {
            dismissDialogFragment(TAG_FATAL_MESSAGE_DIALOG);
        }

        if (viewState instanceof StorageInitializedState) {
            StorageInitializedState state = (StorageInitializedState) viewState;

            if (listener != null) {
                listener.onStorageInitialized();
                viewModel.onStorageInitializedStateReported();
            } else {
                throw new IllegalStateException("Nobody is listening");
            }
        }

        if (viewState instanceof InitStorageFragmentViewState.StorageInitializationFailedState) {
            if (listener != null) {
                listener.onStorageInitializationFailed();
                viewModel.onStorageInitializationFailedStateReported();
            } else {
                throw new IllegalStateException("Nobody is listening");
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

    @Override
    public void onStorageSelected(Storage selectedStorage) {
        viewModel.onStorageSelected(selectedStorage);
    }

    @Override
    public void onOkMessageDialogDismiss() {
        viewModel.onSpaceRequirementWarningDismissed();
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

    private void showPositiveNegativeButtonMessageDialog(ShowPositiveNegativeButtonMessageDialogState state, String tag) {
        PositiveNegativeButtonMessageDialog dialog = findFragment(tag);

        if (dialog == null) {
            dialog = PositiveNegativeButtonMessageDialog.newInstance(state.dialogInfo);
            show(dialog, tag);
        }

        dialog.setListener(new PositiveNegativeButtonMessageDialog.Listener() {
            @Override
            public void onPositiveButtonClicked(boolean neverAskAgain) {
                viewModel.onPositiveNegativeButtonMessageDialogDismissed(true, neverAskAgain);
            }

            @Override
            public void onNegativeButtonClicked(boolean neverAskAgain) {
                viewModel.onPositiveNegativeButtonMessageDialogDismissed(false, neverAskAgain);
            }

            /*
            @Override
            public void onDialogCancelled() {
                viewModel.onPositiveNegativeButtonMessageDialogCancelled();
            }
            */
        });
    }

    private void showFatalMessageDialogState(ShowFatalMessageDialogState state, String tag) {
        FatalMessageDialog dialog = findFragment(tag);

        if (dialog != null) {
            dialog = FatalMessageDialog.newInstance(state.message);
            show(dialog, tag);
        }

        dialog.setListener(new FatalMessageDialog.FatalMessageDialogListener() {
            @Override
            public void onFatalMessageDialogDismissed() {
                viewModel.onFatalMessageDialogDismissed();
            }
        });
    }
}
