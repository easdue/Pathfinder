package nl.erikduisters.pathfinder.ui.dialog.import_settings;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.util.List;

import javax.annotation.Nonnull;

import butterknife.BindView;
import nl.erikduisters.pathfinder.R;
import nl.erikduisters.pathfinder.service.gpsies_service.SearchTracks;
import nl.erikduisters.pathfinder.ui.BaseDialogFragment;
import nl.erikduisters.pathfinder.ui.dialog.CancelMessageDialog;
import nl.erikduisters.pathfinder.ui.dialog.import_settings.ImportSettingsDialogViewState.DismissDialogState;
import nl.erikduisters.pathfinder.ui.dialog.import_settings.ImportSettingsDialogViewState.InitializedState;
import nl.erikduisters.pathfinder.ui.dialog.import_settings.ImportSettingsDialogViewState.ShowCancelMessageDialogState;
import nl.erikduisters.pathfinder.util.menu.MyMenu;
import timber.log.Timber;

/**
 * Created by Erik Duisters on 06-08-2018.
 */
public class ImportSettingsDialog
        extends BaseDialogFragment<ImportSettingsDialogViewModel>
        implements Toolbar.OnMenuItemClickListener {
    private static final String TAG_CANCEL_DIALOG = "CancelDialog";

    public interface Listener {
        void onImportSettingsDialogDismissed(SearchTracks.JobInfo jobInfo);
        void onImportSettingsDialogDismissed(List<File> filesToImport);
        void onImportSettingsDialogCancelled();
    }

    private static final String KEY_VIEW_MODEL_STATE = "ImportSettingsDialogViewModelState";

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.recyclerView) RecyclerView recyclerView;

    private ImportSettingsAdapter adapter;
    private MyMenu optionsMenu;
    private ImportSettingsAdapterData importSettingsAdapterData;
    private Listener listener;

    public static ImportSettingsDialog newInstance() {
        return new ImportSettingsDialog();
    }

    public void setListener(@Nullable Listener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Timber.d("OnCreate()");

        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogTheme);

        if (savedInstanceState != null) {
            viewModel.onRestoreInstanceState(savedInstanceState.getParcelable(KEY_VIEW_MODEL_STATE));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);

        toolbar.setTitle(R.string.import_settings_dialog_title);
        toolbar.setNavigationIcon(R.drawable.abc_ic_clear_material);
        toolbar.setNavigationOnClickListener(v1 -> {
            onCancel(getDialog());

            dismiss();
        });

        toolbar.inflateMenu(R.menu.import_dialog_menu);
        toolbar.setOnMenuItemClickListener(this);

        setupRecyclerView(getContext());

        viewModel.getViewStateObservable().observe(this, this::render);

        return v;
    }

    private void setupRecyclerView(Context context) {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        linearLayoutManager.setSmoothScrollbarEnabled(true);

        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new ImportSettingsItemDecoration(context));
        recyclerView.setClickable(true);

        adapter = new ImportSettingsAdapter(context);
        adapter.setOnChangedListener(changedGroupEntry -> viewModel.onChanged(changedGroupEntry));
        adapter.setOnItemExpandListener(item -> viewModel.onGroupExpanded((ImportSettingsAdapterData.Group) item));
        adapter.setOnItemCollapseListener(item -> viewModel.onGroupCollapsed((ImportSettingsAdapterData.Group) item));

        recyclerView.setAdapter(adapter);
    }

    @Override
    public
    @NonNull
    Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        dialog.setOnCancelListener(this);

        return dialog;
    }

    private void render(@Nullable ImportSettingsDialogViewState viewState) {
        if (viewState == null) {
            return;
        }

        if (viewState instanceof InitializedState) {
            render((InitializedState) viewState);
        }

        if (viewState instanceof ShowCancelMessageDialogState) {
            showCancelMessageDialog((ShowCancelMessageDialogState) viewState, TAG_CANCEL_DIALOG);
        } else {
            dismissDialogFragment(TAG_CANCEL_DIALOG);
        }

        if (viewState instanceof DismissDialogState) {
            render((DismissDialogState) viewState);
        }
    }

    private void render(@Nonnull InitializedState state) {
        render(state.optionsMenu);
        handle(state.importSettingsAdapterData);
    }

    private void render(@NonNull MyMenu optionsMenu) {
        if (optionsMenu != this.optionsMenu) {
            this.optionsMenu = optionsMenu;
            optionsMenu.updateAndroidMenu(toolbar.getMenu(), getContext());
        }
    }

    private void handle(@NonNull ImportSettingsAdapterData importSettingsAdapterData) {
        if (importSettingsAdapterData != this.importSettingsAdapterData) {
            this.importSettingsAdapterData = importSettingsAdapterData;
            adapter.setData(importSettingsAdapterData);
        }
    }

    private void render(DismissDialogState state) {
        if (state instanceof DismissDialogState.ReportSearchTracksState) {
            if (listener != null) {
                listener.onImportSettingsDialogDismissed(((DismissDialogState.ReportSearchTracksState)state).jobInfo);
            }
        }

        if (state instanceof DismissDialogState.ReportImportFilesState) {
            if (listener != null) {
                listener.onImportSettingsDialogDismissed(((DismissDialogState.ReportImportFilesState)state).filesToImport);
            }
        }

        dismiss();
    }

    private void showCancelMessageDialog(ShowCancelMessageDialogState state, String tag) {
        CancelMessageDialog dialog = findFragment(tag);

        if (dialog == null) {
            dialog = CancelMessageDialog.newInstance(state.messageWithTitle);

            show(dialog, tag);
        }

        dialog.setListener(new CancelMessageDialog.CancelMessageDialogListener() {
            @Override
            public void onCancelMessageDialogDismiss() {
                viewModel.onCancelMessageDialogDismissed();
            }
        });
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        Timber.d("onCancel()");
        if (listener != null) {
            listener.onImportSettingsDialogCancelled();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(KEY_VIEW_MODEL_STATE, viewModel.onSaveInstanceState());
    }

    @Override
    public void onDestroyView() {
        adapter.closePopups();

        super.onDestroyView();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_search:
            case R.id.menu_import:
                viewModel.onMenuItemSelected(optionsMenu.findItem(item.getItemId()));
                return true;
        }

        return false;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.dialog_import_settings;
    }

    @Override
    protected Class<ImportSettingsDialogViewModel> getViewModelClass() {
        return ImportSettingsDialogViewModel.class;
    }
}
