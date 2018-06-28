package nl.erikduisters.pathfinder.ui.activity.main_activity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import de.hdodenhof.circleimageview.CircleImageView;
import nl.erikduisters.pathfinder.R;
import nl.erikduisters.pathfinder.ui.BaseActivity;
import nl.erikduisters.pathfinder.ui.MyMenuItem;
import nl.erikduisters.pathfinder.ui.RequestCode;
import nl.erikduisters.pathfinder.ui.activity.FragmentAdapter;
import nl.erikduisters.pathfinder.ui.activity.main_activity.MainActivityViewState.AskUserToEnableGpsState;
import nl.erikduisters.pathfinder.ui.activity.main_activity.MainActivityViewState.CheckPlayServicesAvailabilityState;
import nl.erikduisters.pathfinder.ui.activity.main_activity.MainActivityViewState.FinishState;
import nl.erikduisters.pathfinder.ui.activity.main_activity.MainActivityViewState.InitDatabaseState;
import nl.erikduisters.pathfinder.ui.activity.main_activity.MainActivityViewState.InitStorageViewState;
import nl.erikduisters.pathfinder.ui.activity.main_activity.MainActivityViewState.InitializedState;
import nl.erikduisters.pathfinder.ui.activity.main_activity.MainActivityViewState.RequestRuntimePermissionState;
import nl.erikduisters.pathfinder.ui.activity.main_activity.MainActivityViewState.ShowEnableGpsSettingState;
import nl.erikduisters.pathfinder.ui.activity.main_activity.MainActivityViewState.ShowFatalErrorMessageState;
import nl.erikduisters.pathfinder.ui.activity.main_activity.MainActivityViewState.ShowMessageState;
import nl.erikduisters.pathfinder.ui.activity.main_activity.MainActivityViewState.WaitingForGpsToBeEnabledState;
import nl.erikduisters.pathfinder.ui.dialog.FatalMessageDialog;
import nl.erikduisters.pathfinder.ui.dialog.MessageWithTitle;
import nl.erikduisters.pathfinder.ui.dialog.PositiveNegativeButtonMessageDialog;
import nl.erikduisters.pathfinder.ui.dialog.ProgressDialog;
import nl.erikduisters.pathfinder.ui.fragment.init_storage.InitStorageFragment;
import nl.erikduisters.pathfinder.ui.fragment.play_services.PlayServicesFragment;
import nl.erikduisters.pathfinder.ui.fragment.runtime_permission.RuntimePermissionFragment;
import nl.erikduisters.pathfinder.ui.fragment.runtime_permission.RuntimePermissionFragmentViewModel;
import timber.log.Timber;

//TODO: Remove fragment listeners
public class MainActivity
        extends BaseActivity<MainActivityViewModel>
        implements NavigationView.OnNavigationItemSelectedListener, InitStorageFragment.InitStorageFragmentListener,
        FatalMessageDialog.FatalMessageDialogListener, RuntimePermissionFragment.RuntimePermissionFragmentListener,
        PlayServicesFragment.PlayServicesFragmentListener, PositiveNegativeButtonMessageDialog.Listener {

    private static final String TAG_INIT_STORAGE_FRAGMENT = "InitStorageFragment";
    private static final String TAG_RUNTIME_PERMISSION_FRAGMENT = "RuntimePermissionFragment";
    private static final String TAG_PLAY_SERVICES_AVAILABIITY_FRAGMENT = "PlayServicesAvailablilityFragment";
    private static final String TAG_FATAL_MESSAGE_DIALOG = "FatalMessageDialog";
    private static final String TAG_INIT_DATABASE_PROGRESS_DIALOG = "InitDatabaseProgressDialog";
    private static final String TAG_ASK_USER_TO_ENABLE_GPS_DIALOG = "AskUserToEnableGpsDialog";

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.drawer_layout) DrawerLayout drawerLayout;
    @BindView(R.id.nav_view) NavigationView navigationView;
    @BindView(R.id.tabLayout) TabLayout tabLayout;
    @BindView(R.id.viewPager) ViewPager viewPager;

    private CircleImageView avatar;
    private TextView username;
    private Menu navigationMenu;
    private @NonNull List<MyMenuItem> optionsMenu;
    private FragmentAdapter fragmentAdapter;

    public MainActivity() {
        optionsMenu = new ArrayList<>();
    }

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

        navigationMenu = navigationView.getMenu();

        viewModel.getMainActivityViewStateObservable().observe(this, this::render);
        viewModel.getNavigationViewStateObservable().observe(this, this::render);

        fragmentAdapter = new FragmentAdapter(getSupportFragmentManager(), this);
        viewPager.setAdapter(fragmentAdapter);
        tabLayout.setupWithViewPager(viewPager);
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
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        for (MyMenuItem item : optionsMenu) {
            updateMenu(menu, item);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

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

    void render(@Nullable NavigationViewState viewState) {
        Timber.d("render(NavigationViewState == %s", viewState == null ? "null" : viewState.getClass().getSimpleName());

        if (viewState == null) {
            return;
        }

        avatar.setImageDrawable(viewState.avatar.getDrawable(this));
        username.setText(viewState.userName.getString(this));

        for (MyMenuItem item : viewState.navigationMenu) {
            updateMenu(navigationMenu, item);
        }
    }

    private void updateMenu(Menu menu, MyMenuItem myItem) {
        MenuItem item = menu.findItem(myItem.id);

        if (item != null) {
            item.setEnabled(myItem.enabled);
            item.setVisible(myItem.visible);
        }
    }

    void render(@Nullable MainActivityViewState viewState) {
        Timber.e("render(viewState == %s)", viewState == null ? "null" : viewState.getClass().getSimpleName());

        if (viewState == null) {
            return;
        }

        if (viewState instanceof InitDatabaseState) {
            showInitDatabaseProgressDialog(TAG_INIT_DATABASE_PROGRESS_DIALOG, (InitDatabaseState) viewState);
        } else {
            dismissDialogFragment(TAG_INIT_DATABASE_PROGRESS_DIALOG);
        }

        if (viewState instanceof InitStorageViewState) {
            startInitStorageFragment(TAG_INIT_STORAGE_FRAGMENT);
        } else {
            removeFragment(TAG_INIT_STORAGE_FRAGMENT);
        }

        if (viewState instanceof RequestRuntimePermissionState) {
            startRuntimePermissionFragment(TAG_RUNTIME_PERMISSION_FRAGMENT, (RequestRuntimePermissionState)viewState);
        } else {
            removeFragment(TAG_RUNTIME_PERMISSION_FRAGMENT);
        }

        if (viewState instanceof CheckPlayServicesAvailabilityState) {
            startPlayServicesAvailabilityFragment(TAG_PLAY_SERVICES_AVAILABIITY_FRAGMENT);
        } else {
            removeFragment(TAG_PLAY_SERVICES_AVAILABIITY_FRAGMENT);
        }

        if (viewState instanceof ShowMessageState) {
            showSnackbar(((ShowMessageState) viewState).message);
            viewModel.onMessageDismissed();
        }

        if (viewState instanceof ShowFatalErrorMessageState) {
            showFatalMessageDialog(TAG_FATAL_MESSAGE_DIALOG, ((ShowFatalErrorMessageState) viewState).message);
        } else {
            dismissDialogFragment(TAG_FATAL_MESSAGE_DIALOG);
        }

        if (viewState instanceof FinishState) {
            finish();
        }

        if (viewState instanceof AskUserToEnableGpsState) {
            showAskUserToEnableGpsDialog((AskUserToEnableGpsState) viewState, TAG_ASK_USER_TO_ENABLE_GPS_DIALOG);
        } else {
            dismissDialogFragment(TAG_ASK_USER_TO_ENABLE_GPS_DIALOG);
        }

        if (viewState instanceof ShowEnableGpsSettingState) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(intent, RequestCode.ENABLE_GPS);
            viewModel.onWaitingForGpsToBeEnabled();
        }

        if (viewState instanceof WaitingForGpsToBeEnabledState) {
            //Do nothing
        }

        if (viewState instanceof InitializedState) {
            render((InitializedState) viewState);
        }
    }

    private void render(InitializedState state) {
        optionsMenu = state.optionsMenu;
        invalidateOptionsMenu();

        initViewPager();
    }

    private void initViewPager() {
        fragmentAdapter.addTab(new FragmentAdapter.TabItem(R.string.track_list, true, MainActivityFragmentProvider.TRACK_LIST_FRAGMENT));
        fragmentAdapter.addTab(new FragmentAdapter.TabItem(R.string.map, true, MainActivityFragmentProvider.MAP_FRAGMENT));
        fragmentAdapter.addTab(new FragmentAdapter.TabItem(R.string.compass, true, MainActivityFragmentProvider.COMPASS_FRAGMENT));
    }

    private void startInitStorageFragment(String tag) {
        InitStorageFragment fragment = findFragment(tag);

        if (fragment == null) {
            fragment = new InitStorageFragment();

            addFragment(fragment, tag);
        }

        fragment.setListener(this);
    }

    private void startRuntimePermissionFragment(String tag, RequestRuntimePermissionState viewState) {
        RuntimePermissionFragment fragment = findFragment(tag);

        if (fragment == null) {
            fragment = RuntimePermissionFragment.newInstance(viewState.request);

            addFragment(fragment, tag);
        } else {
            RuntimePermissionFragmentViewModel viewModel = fragment.getViewModel();

            if (viewModel != null) viewModel.requestPermission(viewState.request);
        }

        fragment.setListener(this);
    }

    private void startPlayServicesAvailabilityFragment(String tag) {
        PlayServicesFragment fragment = findFragment(tag);

        if (fragment == null) {
            fragment = PlayServicesFragment.newInstance();

            addFragment(fragment, tag);
        }

        fragment.setListener(this);
    }

    private void showInitDatabaseProgressDialog(String tag, InitDatabaseState state) {
        ProgressDialog dialog = findFragment(tag);

        if (dialog == null) {
            dialog = ProgressDialog.newInstance(state.dialogProperties);
            show(dialog, tag);
        }

        if (state.progress != null) {
            dialog.setProgressAndMessage(state.progress.progress, state.progress.message);
        }
    }

    @Override
    public void onStorageInitialized() {
        viewModel.onStorageInitialized();
    }

    @Override
    public void onStorageInitializationFailed(MessageWithTitle message, boolean isFatal) {
        if (isFatal)
            viewModel.handleFatalError(message, null);
        else
            viewModel.handleMessage(message);
    }

    private void showFatalMessageDialog(String tag, MessageWithTitle message) {
        FatalMessageDialog dialog = findFragment(tag);

        if (dialog == null) {
            dialog = FatalMessageDialog.newInstance(message);

            show(dialog, tag);
        }

        dialog.setListener(this);
    }

    private void showAskUserToEnableGpsDialog(AskUserToEnableGpsState state, String tag) {
        PositiveNegativeButtonMessageDialog dialog = findFragment(tag);

        if (dialog == null) {
            dialog = PositiveNegativeButtonMessageDialog.newInstance(state.message, state.showNeverAskAgain, state.positiveButtonTextResId, state.negativeButtonTextResId, tag);

            show(dialog, tag);
        }

        dialog.setListener(this);
    }

    @Override
    public void onFatalMessageDialogDismissed() {
        viewModel.onFatalErrorMessageDismissed();
    }

    private void showSnackbar(MessageWithTitle message) {
        StringBuilder sb = new StringBuilder();

        sb.append(getString(message.titleResId));

        if (message.messageResId != 0) {
            sb.append("\n");
            sb.append(getString(message.messageResId));
        } else if (!message.message.isEmpty()) {
            sb.append("\n");
            sb.append(message.message);
        }

        Snackbar.make(viewPager, sb.toString(), Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionGranted(@NonNull String permission) {
        viewModel.onPermissionGranted(permission);
    }

    @Override
    public void onPermissionDenied(@NonNull String permission) {
        viewModel.onPermissionDenied(permission);
    }

    @Override
    public void onPlayServicesAvailable() {
        viewModel.onPlayServicesAvailable();
    }

    @Override
    public void onPlayServicesUnavailable() {
        viewModel.onPlayServicesUnavailable();
    }

    @Override
    public void onPositiveButtonClicked(String tag, boolean neverAskAgain) {
        viewModel.onUserWantsToEnableGps(neverAskAgain);
    }

    @Override
    public void onNegativeButtonClicked(String tag, boolean neverAskAgain) {
        viewModel.onUserDoesNotWantToEnableGps(neverAskAgain);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RequestCode.ENABLE_GPS:
                viewModel.onFinishedWaitingForGpsToBeEnabled();
                break;
        }
    }
}
