package nl.erikduisters.pathfinder.ui.dialog.select_tracks_to_import;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import nl.erikduisters.pathfinder.R;
import nl.erikduisters.pathfinder.data.model.Marker;
import nl.erikduisters.pathfinder.service.gpsies_service.GPSiesService;
import nl.erikduisters.pathfinder.service.gpsies_service.Result;
import nl.erikduisters.pathfinder.service.gpsies_service.SearchTracks;
import nl.erikduisters.pathfinder.ui.BaseDialogFragment;
import nl.erikduisters.pathfinder.ui.dialog.select_tracks_to_import.SelectTracksToImportDialogViewState.DataState;
import nl.erikduisters.pathfinder.ui.dialog.select_tracks_to_import.SelectTracksToImportDialogViewState.DisplayMessageState;
import nl.erikduisters.pathfinder.ui.dialog.select_tracks_to_import.SelectTracksToImportDialogViewState.DisplayShortMessageState;
import nl.erikduisters.pathfinder.ui.dialog.select_tracks_to_import.SelectTracksToImportDialogViewState.ReportSelectedTracksState;
import nl.erikduisters.pathfinder.ui.dialog.select_tracks_to_import.SelectTracksToImportDialogViewState.ShowProgressState;
import nl.erikduisters.pathfinder.ui.dialog.select_tracks_to_import.SelectTracksToImportDialogViewState.StartTrackSearchState;
import nl.erikduisters.pathfinder.ui.widget.SvgView;
import nl.erikduisters.pathfinder.util.menu.MyMenu;
import timber.log.Timber;

/**
 * Created by Erik Duisters on 12-08-2018.
 */
public class SelectTracksToImportDialog
        extends BaseDialogFragment<SelectTracksToImportDialogViewModel>
        implements Toolbar.OnMenuItemClickListener {
    private static final String KEY_JOB_INFO = "JobInfo";
    private static final String KEY_VIEW_MODEL_STATE = "ViewModelState";
    private static final String KEY_ADAPTER_STATE = "AdapterState";

    public interface Listener {
        void onSelectTracksToImportDialogDismissed(List<String> trackFileIds);
        void onSelectTracksToImportDialogCancelled();
    }

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.recyclerView) RecyclerView recyclerView;
    @BindView(R.id.progressBar) ProgressBar progressBar;
    @BindView(R.id.progressMessage) TextView progressMessage;
    @BindView(R.id.retrySvgView) SvgView retrySvgView;

    @NonNull private MyMenu optionsMenu;
    @Nullable private Listener listener;
    final private GPSiesServiceBroadcastReceiver broadcastReceiver;
    @Nullable List<Marker> markers;
    private MarkerAdapter markerAdapter;

    public static SelectTracksToImportDialog newInstance(SearchTracks.JobInfo jobInfo) {
        SelectTracksToImportDialog dialog = new SelectTracksToImportDialog();

        Bundle args = new Bundle();
        args.putParcelable(KEY_JOB_INFO, jobInfo);

        dialog.setArguments(args);

        return dialog;
    }

    public SelectTracksToImportDialog() {
        broadcastReceiver = new GPSiesServiceBroadcastReceiver();
    }

    public void setListener(@Nullable Listener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Timber.d("OnCreate()");

        optionsMenu = new MyMenu();

        Bundle args = getArguments();

        markerAdapter = new MarkerAdapter(requireContext());

        if (savedInstanceState != null) {
            viewModel.onRestoreInstanceState(savedInstanceState.getParcelable(KEY_VIEW_MODEL_STATE));
            markerAdapter.onRestoreInstanceState(savedInstanceState.getParcelable(KEY_ADAPTER_STATE));
        }

        if (args == null || !args.containsKey(KEY_JOB_INFO)) {
            throw new IllegalStateException("You have to instantiate a new SelectTracksToImportDialog using newInstance()");
        }

        SearchTracks.JobInfo jobInfo = args.getParcelable(KEY_JOB_INFO);

        viewModel.set(jobInfo);

        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogTheme);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);

        toolbar.setTitle(R.string.select_tracks_to_import_dialog_title);
        toolbar.setNavigationIcon(R.drawable.abc_ic_clear_material);
        toolbar.setNavigationOnClickListener(v1 -> {
            onCancel(getDialog());

            dismiss();
        });

        toolbar.inflateMenu(R.menu.import_dialog_menu);
        toolbar.setOnMenuItemClickListener(this);

        setupRecyclerView();

        retrySvgView.setOnClickListener(v12 -> {
            viewModel.onRetryClicked();
        });

        viewModel.getViewStateObservable().observe(this, this::render);
        viewModel.getDisplayShortMessageObservable().observe(this, this::render);

        return v;
    }

    private void setupRecyclerView() {
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false);
        layoutManager.setSmoothScrollbarEnabled(true);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(itemDecoration);
        recyclerView.setAdapter(markerAdapter);
    }

    private void render(@Nullable SelectTracksToImportDialogViewState viewState) {
        if (viewState == null) {
            return;
        }

        if (viewState instanceof ShowProgressState) {
            handleOptionsMenu(((ShowProgressState) viewState).optionsMenu);
            showProgress((ShowProgressState) viewState);
        } else {
            hideProgress();
        }

        if (viewState instanceof StartTrackSearchState) {
            startTrackSearch((StartTrackSearchState) viewState);
        }

        if (viewState instanceof DisplayMessageState) {
            progressBar.setVisibility(View.GONE);
            retrySvgView.setVisibility(((DisplayMessageState)viewState).isRetryable() ? View.VISIBLE : View.GONE);
        } else {
            retrySvgView.setVisibility(View.GONE);
        }

        if (viewState instanceof DataState) {
            render((DataState) viewState);
        }

        if (viewState instanceof ReportSelectedTracksState) {
            if (listener != null) {
                listener.onSelectTracksToImportDialogDismissed(((ReportSelectedTracksState) viewState).selectedTrackFileIds);
                viewModel.onSelectedMarkersReported();
                dismiss();
            }
        }
    }

    private void handleOptionsMenu(MyMenu optionsMenu) {
        if (optionsMenu != this.optionsMenu) {
            this.optionsMenu = optionsMenu;
            optionsMenu.updateAndroidMenu(toolbar.getMenu(), requireContext());
        }
    }

    private void render(@Nullable DisplayShortMessageState state) {
        if (state == null) {
            return;
        }

        Snackbar.make(toolbar, state.getMessage(requireContext()), state.displayDuration).show();

        viewModel.onShortMessageDisplayed();
    }

    private void render(DataState state) {
        handleOptionsMenu(state.optionsMenu);

        if (state.markers != markers) {
            markers = state.markers;
            markerAdapter.setMarkers(markers);

            if (markers.size() == 0) {
                progressBar.setVisibility(View.GONE);
                progressMessage.setText(state.emptyListMessage);
                progressMessage.setVisibility(View.VISIBLE);
            }
        }
    }

    private void showProgress(ShowProgressState state) {
        progressBar.setVisibility(View.VISIBLE);
        progressMessage.setText(state.getMessage(getContext()));
        progressMessage.setVisibility(View.VISIBLE);
    }

    private void hideProgress() {
        progressBar.setVisibility(View.INVISIBLE);
        progressMessage.setVisibility(View.INVISIBLE);
    }

    private void startTrackSearch(StartTrackSearchState state) {
        Intent intent = new Intent(requireContext(), GPSiesService.class);
        intent.setAction(GPSiesService.ACTION_SEARCH_TRACKS);
        intent.putExtra(GPSiesService.EXTRA_SEARCH_JOB_INFO, state.jobInfo);

        requireContext().startService(intent);
    }

    @Override
    public void onStart() {
        super.onStart();

        viewModel.setSelectedMarkerProvider(markerAdapter);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(GPSiesService.BROADCAST_ACTION_RESULT);

        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onStop() {
        super.onStop();

        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(broadcastReceiver);

        viewModel.setSelectedMarkerProvider(null);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.dialog_select_tracks_to_import;
    }

    @Override
    protected Class<SelectTracksToImportDialogViewModel> getViewModelClass() {
        return SelectTracksToImportDialogViewModel.class;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);

        if (listener != null) {
            listener.onSelectTracksToImportDialogCancelled();
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_import:
                viewModel.onMenuItemSelected(optionsMenu.findItem(item.getItemId()));
        }
        return false;
    }

    private class GPSiesServiceBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null && intent.getAction().equals(GPSiesService.BROADCAST_ACTION_RESULT)) {
                Result result = intent.getParcelableExtra(GPSiesService.EXTRA_RESULT);

                viewModel.onResult(result);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(KEY_ADAPTER_STATE, markerAdapter.onSaveInstanceState());
        outState.putParcelable(KEY_VIEW_MODEL_STATE, viewModel.onSaveInstanceState());
    }
}
