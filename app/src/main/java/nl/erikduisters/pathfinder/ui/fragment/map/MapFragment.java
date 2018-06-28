package nl.erikduisters.pathfinder.ui.fragment.map;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import nl.erikduisters.pathfinder.R;
import nl.erikduisters.pathfinder.ui.BaseFragment;

/**
 * Created by Erik Duisters on 28-06-2018.
 */
//TODO: Implement
public class MapFragment extends BaseFragment<MapFragmentViewModel> {
    @BindView(R.id.textView) TextView textview;

    public static MapFragment newInstance() {
        return new MapFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);

        textview.setText(R.string.map);

        return v;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_map;
    }

    @Override
    protected Class<MapFragmentViewModel> getViewModelClass() {
        return MapFragmentViewModel.class;
    }

}
