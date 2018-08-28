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

package nl.erikduisters.pathfinder.ui.fragment.track_list;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import butterknife.BindView;
import nl.erikduisters.pathfinder.R;
import nl.erikduisters.pathfinder.data.model.MinimalTrack;
import nl.erikduisters.pathfinder.ui.BaseFragment;
import nl.erikduisters.pathfinder.ui.fragment.ViewPagerFragment;
import nl.erikduisters.pathfinder.ui.fragment.track_list.TrackListFragmentViewState.DataState;
import nl.erikduisters.pathfinder.ui.fragment.track_list.TrackListFragmentViewState.LoadingState;
import nl.erikduisters.pathfinder.ui.fragment.track_list.TrackListFragmentViewState.NoTracksFoundState;
import timber.log.Timber;

/**
 * Created by Erik Duisters on 28-06-2018.
 */

public class TrackListFragment
        extends BaseFragment<TrackListFragmentViewModel>
        implements ViewPagerFragment, MinimalTrackAdapter.Listener {
    private static final String KEY_VIEWMODEL_STATE = "ViewModelState";
    private static final String KEY_LAYOUT_MANAGER_STATE = "LayoutManagerState";

    @BindView(R.id.recyclerView) RecyclerView recyclerView;
    @BindView(R.id.progressBar) ProgressBar progressBar;
    @BindView(R.id.progressMessage) TextView progressMessage;

    private MinimalTrackAdapter adapter;
    private Parcelable layoutManagerSavedState;
    private LinearLayoutManager layoutManager;

    public static TrackListFragment newInstance() {
        return new TrackListFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        adapter = new MinimalTrackAdapter(requireContext());

        if (savedInstanceState != null) {
            viewModel.onRestoreState(savedInstanceState.getParcelable(KEY_VIEWMODEL_STATE));
            layoutManagerSavedState = savedInstanceState.getParcelable(KEY_LAYOUT_MANAGER_STATE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);

        adapter.setListener(this);
        setupRecyclerView();

        viewModel.getViewStateObservable().observe(this, this::render);

        return v;
    }

    private void setupRecyclerView() {
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL);
        layoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false);
        layoutManager.setSmoothScrollbarEnabled(true);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(itemDecoration);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(KEY_VIEWMODEL_STATE, viewModel.onSaveState());
        outState.putParcelable(KEY_LAYOUT_MANAGER_STATE, recyclerView.getLayoutManager().onSaveInstanceState());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        adapter.setListener(null);
    }

    private void render(TrackListFragmentViewState viewState) {
        if (viewState == null) {
            Timber.d("null received");
            return;
        }

        if (viewState instanceof LoadingState) {
            render((LoadingState) viewState);
        }

        if (viewState instanceof NoTracksFoundState) {
            render((NoTracksFoundState) viewState);
        }

        if (viewState instanceof DataState) {
            render((DataState) viewState);
        }
    }

    private void render(LoadingState state) {
        progressBar.setVisibility(View.VISIBLE);
        progressMessage.setText(getString(state.message));
        progressMessage.setVisibility(View.VISIBLE);
    }

    private void render(NoTracksFoundState state) {
        progressBar.setVisibility(View.GONE);
        progressMessage.setText(getString(state.messageResId, state.distance.getDistance(requireContext())));
        progressMessage.setVisibility(View.VISIBLE);
    }

    private void render(DataState state) {
        progressBar.setVisibility(View.GONE);
        progressMessage.setVisibility(View.GONE);

        adapter.setMinimalTracks(state.minimalTrackList.getMinimalTracks());

        if (state.selectedMinimalTrack != null) {
            adapter.setSelected(state.selectedMinimalTrack);

            int position = state.minimalTrackList.getMinimalTracks().indexOf(state.selectedMinimalTrack);

            if (!completelyVisible(position)) {
                layoutManager.scrollToPosition(position);
            }
        }

        if (layoutManagerSavedState != null) {
            layoutManager.onRestoreInstanceState(layoutManagerSavedState);
            layoutManagerSavedState = null;
        }
    }

    private boolean completelyVisible(int position) {
        int firstVisible = layoutManager.findFirstCompletelyVisibleItemPosition();
        int lastVisible = layoutManager.findLastCompletelyVisibleItemPosition();

        return firstVisible != RecyclerView.NO_POSITION && (position >= firstVisible && position <= lastVisible);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_track_list;
    }

    @Override
    protected Class<TrackListFragmentViewModel> getViewModelClass() {
        return TrackListFragmentViewModel.class;
    }

    @Override
    public void onVisibilityChanged(boolean visible) {}

    @Override
    public void onItemClicked(MinimalTrack minimalTrack) {
        viewModel.onMinimalTrackClicked(minimalTrack);
    }

    @Override
    public void onItemLongClicked(MinimalTrack minimalTrack) {
        //TODO
    }
}
