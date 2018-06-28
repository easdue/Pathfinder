package nl.erikduisters.pathfinder.ui.fragment.compass;

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
public class CompassFragment extends BaseFragment<CompassFragmentViewModel> {
    @BindView(R.id.textView) TextView textview;

    public static CompassFragment newInstance() {
        return new CompassFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);

        textview.setText(R.string.compass);

        return v;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_compass;
    }

    @Override
    protected Class<CompassFragmentViewModel> getViewModelClass() {
        return CompassFragmentViewModel.class;
    }
}
