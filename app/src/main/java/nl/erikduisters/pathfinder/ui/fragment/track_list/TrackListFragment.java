package nl.erikduisters.pathfinder.ui.fragment.track_list;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import butterknife.BindView;
import nl.erikduisters.pathfinder.R;
import nl.erikduisters.pathfinder.ui.BaseFragment;
import nl.erikduisters.pathfinder.ui.activity.ViewPagerFragment;

/**
 * Created by Erik Duisters on 28-06-2018.
 */

//TODO: Implement
public class TrackListFragment
        extends BaseFragment<TrackListFragmentViewModel>
        implements ViewPagerFragment {
    @BindView(R.id.recyclerView) RecyclerView recyclerView;
    @BindView(R.id.progressBar) ProgressBar progressBar;
    @BindView(R.id.progressMessage) TextView progressMessage;

    public static TrackListFragment newInstance() {
        return new TrackListFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);

        progressBar.setVisibility(View.GONE);
        progressMessage.setText(R.string.track_list);

        return v;
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
    public void onVisibilityChanged(boolean visible) {
        //TODO:
    }
}
