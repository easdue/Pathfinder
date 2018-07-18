package nl.erikduisters.pathfinder.ui.activity.gps_status;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import butterknife.BindView;
import nl.erikduisters.pathfinder.R;
import nl.erikduisters.pathfinder.ui.BaseActivity;
import nl.erikduisters.pathfinder.ui.fragment.gps_status.GpsStatusFragment;
import nl.erikduisters.pathfinder.ui.fragment.runtime_permission.RuntimePermissionHelper;

/**
 * Created by Erik Duisters on 17-07-2018.
 */
public class GpsStatusActivity
        extends BaseActivity<GpsStatusActivityViewModel>
        implements RuntimePermissionHelper {
    @BindView(R.id.toolbar) Toolbar toolbar;
    private GpsStatusFragment gpsStatusFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        gpsStatusFragment = findFragment(R.id.gpsStatusFragment);

        viewModel.setRuntimePermissionHelper(this);

        viewModel.getViewStateObservable().observe(this, this::render);
    }

    private void render(@Nullable GpsStatusActivityViewState viewState) {
        if (viewState == null) {
            return;
        }

        if (viewState instanceof GpsStatusActivityViewState.FinishState) {
            finish();
        }

        if (viewState instanceof GpsStatusActivityViewState.InitializedState) {
            //Do nothing
        }
    }

    @Override
    public void onBackPressed() {
        gpsStatusFragment.onFinish();
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                gpsStatusFragment.onFinish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_gps_status;
    }

    @Override
    protected Class<GpsStatusActivityViewModel> getViewModelClass() {
        return GpsStatusActivityViewModel.class;
    }

    @Override
    public boolean hasPermission(String permission) {
        //noinspection ConstantConditions
        return ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public boolean shouldShowPermissionRationale(String permission) {
        return false;
    }
}
