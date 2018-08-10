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

import butterknife.BindView;
import nl.erikduisters.pathfinder.R;
import nl.erikduisters.pathfinder.ui.BaseDialogFragment;
import nl.erikduisters.pathfinder.util.menu.MyMenu;
import timber.log.Timber;

/**
 * Created by Erik Duisters on 06-08-2018.
 */
public class ImportSettingsDialog
        extends BaseDialogFragment<ImportSettingsDialogViewModel>
        implements Toolbar.OnMenuItemClickListener {

    private static final String KEY_MAP_BOUNDING_BOX = "MapBoundingBox";
    private static final String KEY_VIEW_MODEL_STATE = "ImportSettingsDialogViewModelState";

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.recyclerView) RecyclerView recyclerView;

    private ImportSettingsAdapter adapter;

    public static ImportSettingsDialog newInstance() {
        return new ImportSettingsDialog();
    }

    private MyMenu optionsMenu;
    private ImportSettingsAdapterData importSettingsAdapterData;

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
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCancel(getDialog());

                dismiss();
            }
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

        handle(viewState.optionsMenu);
        handle(viewState.importSettingsAdapterData);
    }

    private void handle(@NonNull MyMenu optionsMenu) {
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

    @Override
    public void onCancel(DialogInterface dialog) {
        Timber.d("onCancel()");
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
