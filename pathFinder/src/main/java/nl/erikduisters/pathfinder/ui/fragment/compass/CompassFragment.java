package nl.erikduisters.pathfinder.ui.fragment.compass;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import nl.erikduisters.pathfinder.R;
import nl.erikduisters.pathfinder.ui.BaseFragment;
import nl.erikduisters.pathfinder.ui.fragment.ViewPagerFragment;
import nl.erikduisters.pathfinder.ui.widget.CompassView;
import nl.erikduisters.pathfinder.util.menu.MyMenu;

/**
 * Created by Erik Duisters on 28-06-2018.
 */

public class CompassFragment
        extends BaseFragment<CompassFragmentViewModel>
        implements ViewPagerFragment {
    @BindView(R.id.speed) TextView speed;
    @BindView(R.id.distanceToNext) TextView distanceToNext;
    @BindView(R.id.compass) CompassView compass;

    @NonNull private MyMenu optionsMenu;

    public static CompassFragment newInstance() {
        return new CompassFragment();
    }

    public CompassFragment() { optionsMenu = new MyMenu(); }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);

        viewModel.getViewStateObservable().observe(this, this::render);

        return v;
    }

    private void render(CompassFragmentViewState viewState) {
        if (viewState == null) {
            return;
        }

        if (viewState.optionsMenu != optionsMenu) {
            optionsMenu = viewState.optionsMenu;
            invalidateOptionsMenu();
        }

        speed.setText(viewState.speed.getSpeed(getContext()));
        distanceToNext.setText(viewState.distanceToNext.getDistance(getContext()));

        compass.setBearing(viewState.bearing);
        compass.setHeading(viewState.heading);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_compass;
    }

    @Override
    protected Class<CompassFragmentViewModel> getViewModelClass() {
        return CompassFragmentViewModel.class;
    }

    @Override
    public void onVisibilityChanged(boolean visible) {
        if (visible) {
            viewModel.start();
        } else {
            viewModel.stop();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        //TODO
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        //TODO
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //TODO

        return false;
    }
}
