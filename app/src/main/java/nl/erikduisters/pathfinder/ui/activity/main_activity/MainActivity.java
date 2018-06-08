package nl.erikduisters.pathfinder.ui.activity.main_activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import de.hdodenhof.circleimageview.CircleImageView;
import nl.erikduisters.pathfinder.R;
import nl.erikduisters.pathfinder.ui.BaseActivity;
import nl.erikduisters.pathfinder.ui.activity.main_activity.MainActivityViewState.InitStorageViewState;
import nl.erikduisters.pathfinder.ui.activity.main_activity.MainActivityViewState.ShowMessageState;
import nl.erikduisters.pathfinder.ui.dialog.FatalMessageDialog;
import nl.erikduisters.pathfinder.ui.dialog.MessageWithTitle;
import nl.erikduisters.pathfinder.ui.fragment.init_storage.InitStorageFragment;

public class MainActivity extends BaseActivity<MainActivityViewModel>
        implements NavigationView.OnNavigationItemSelectedListener, InitStorageFragment.InitStorageFragmentListener, FatalMessageDialog.FatalMessageDialogListener {

    private static final String TAG_INIT_STORAGE_FRAGMENT = "InitStorageFragment";
    private static final String TAG_MESSAGE_DIALOG = "MessageDialog";

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.drawer_layout) DrawerLayout drawerLayout;
    @BindView(R.id.nav_view) NavigationView navigationView;
    @BindView(R.id.constraintLayout) ConstraintLayout constraintLayout;
    CircleImageView avatar;
    TextView username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        View headerView = navigationView.getHeaderView(0);
        avatar = headerView.findViewById(R.id.gpsies_avatar);
        username = headerView.findViewById(R.id.gpsies_username);

        //TODO: Move this to render
        VectorDrawableCompat avatarVectorDrawable = VectorDrawableCompat.create(getResources(), R.drawable.vector_drawable_ic_missing_avatar, null);
        avatar.setImageDrawable(avatarVectorDrawable);
        username.setText("Please login");

        viewModel.getMainActivityViewState().observe(this, this::render);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_main;
    }

    @Override
    protected Class<MainActivityViewModel> getViewModelClass() {
        return MainActivityViewModel.class;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        switch (item.getItemId()) {
            case R.id.nav_import:
                break;
            case R.id.nav_login_register:
                break;
            case R.id.nav_gps_status:
                break;
            case R.id.nav_settings:
                break;
        }

        drawerLayout.closeDrawer(GravityCompat.START);

        return false;
    }

    void render(@Nullable MainActivityViewState viewState) {
        if (viewState == null) {
            return;
        }

        if (viewState instanceof InitStorageViewState) {
            startInitStorageFragment(TAG_INIT_STORAGE_FRAGMENT);
        } else {
            removeFragment(TAG_INIT_STORAGE_FRAGMENT);
        }

        if (viewState instanceof ShowMessageState) {
            showMessageDialog(TAG_MESSAGE_DIALOG, (ShowMessageState) viewState);
        } else {
            dismissDialogFragment(TAG_MESSAGE_DIALOG);
        }
    }

    private void startInitStorageFragment(String tag) {
        InitStorageFragment fragment = findFragment(tag);

        if (fragment == null) {
            fragment = new InitStorageFragment();

            addFragment(fragment, TAG_INIT_STORAGE_FRAGMENT);
        }

        fragment.setListener(this);
    }

    @Override
    public void onStorageInitialized() {
        viewModel.onStorageInitialized();
    }

    @Override
    public void onStorageInitializationFailed(MessageWithTitle message, boolean isFatal) {
        viewModel.handleMessage(message, isFatal);
    }

    private void showMessageDialog(String Tag, ShowMessageState state) {
        if (state.isFatal) {
            showFatalMessageDialog(state.message);
        } else {
            showNonFatalMessage(state.message);

            viewModel.onMessageDismissed(state);
        }
    }

    private void showFatalMessageDialog(MessageWithTitle message) {
        FatalMessageDialog dialog = findFragment(TAG_MESSAGE_DIALOG);

        if (dialog == null) {
            dialog = FatalMessageDialog.newInstance(message);

            show(dialog, TAG_MESSAGE_DIALOG);
        }

        dialog.setListener(this);
    }

    @Override
    public void onFatalMessageDialogDismissed() {
        viewModel.onMessageDismissed((ShowMessageState) viewModel.getMainActivityViewState().getValue());
    }

    private void showNonFatalMessage(MessageWithTitle message) {
        StringBuilder sb = new StringBuilder();

        sb.append(getString(message.titleResId));

        if (message.messageResId != 0) {
            sb.append("\n");
            sb.append(getString(message.messageResId));
        } else if (!message.message.isEmpty()) {
            sb.append("\n");
            sb.append(message.message);
        }

        Snackbar.make(constraintLayout, sb.toString(), Snackbar.LENGTH_LONG).show();
    }
}
