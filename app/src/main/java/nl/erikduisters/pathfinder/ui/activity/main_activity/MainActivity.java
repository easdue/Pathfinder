package nl.erikduisters.pathfinder.ui.activity.main_activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
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
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;

import butterknife.BindView;
import de.hdodenhof.circleimageview.CircleImageView;
import nl.erikduisters.pathfinder.R;
import nl.erikduisters.pathfinder.service.MapDownloadService;
import nl.erikduisters.pathfinder.ui.BaseActivity;
import nl.erikduisters.pathfinder.ui.RequestCode;
import nl.erikduisters.pathfinder.ui.activity.FragmentAdapter;
import nl.erikduisters.pathfinder.ui.activity.ViewPagerFragment;
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
import nl.erikduisters.pathfinder.ui.view.MyViewPager;
import nl.erikduisters.pathfinder.util.menu.MyMenu;
import timber.log.Timber;

//TODO: Remove fragment listeners
//TODO: Bottom navigation instead of tabs?
//TODO: Remind users to install an offline map
//TODO: After setting finish state the viewState must be set to null or something else otherwise the app cannot be started again
//TODO: Allow user to configure a new storage location (clear storagedir/cachedir and storageUUID
public class MainActivity
        extends BaseActivity<MainActivityViewModel>
        implements NavigationView.OnNavigationItemSelectedListener, InitStorageFragment.InitStorageFragmentListener,
                   FatalMessageDialog.FatalMessageDialogListener, RuntimePermissionFragment.RuntimePermissionFragmentListener,
                   PlayServicesFragment.PlayServicesFragmentListener,
                   ViewPager.OnPageChangeListener, ViewTreeObserver.OnGlobalLayoutListener{

    private static final String TAG_INIT_STORAGE_FRAGMENT = "InitStorageFragment";
    private static final String TAG_RUNTIME_PERMISSION_FRAGMENT = "RuntimePermissionFragment";
    private static final String TAG_PLAY_SERVICES_AVAILABIITY_FRAGMENT = "PlayServicesAvailablilityFragment";
    private static final String TAG_FATAL_MESSAGE_DIALOG = "FatalMessageDialog";
    private static final String TAG_INIT_DATABASE_PROGRESS_DIALOG = "InitDatabaseProgressDialog";
    private static final String TAG_ASK_USER_TO_ENABLE_GPS_DIALOG = "AskUserToEnableGpsDialog";

    private static final String KEY_CURRENT_VIEWPAGER_POSITION = "CurrentViewpagerPosition";

    public static final String INTENT_EXTRA_STARTED_FROM_MAP_AVAILABLE_NOTIFICATION = "nl.erikduisters.pathfinder.StartedFromMapAvailableNotification";

    @BindView(R.id.coordinatorLayout) CoordinatorLayout coordinatorLayout;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.drawer_layout) DrawerLayout drawerLayout;
    @BindView(R.id.nav_view) NavigationView navigationView;
    @BindView(R.id.tabLayout) TabLayout tabLayout;
    @BindView(R.id.viewPager) MyViewPager viewPager;

    private CircleImageView avatar;
    private TextView username;
    private @NonNull MyMenu navigationMenu;
    private @NonNull MyMenu optionsMenu;
    private FragmentAdapter fragmentAdapter;
    private int currentViewPagerPosition;
    private boolean viewPagerInitialized;

    public MainActivity() {
        navigationMenu = new MyMenu();
        optionsMenu = new MyMenu();
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

        viewModel.getMainActivityViewStateObservable().observe(this, this::render);
        viewModel.getNavigationViewStateObservable().observe(this, this::render);
        viewModel.getStartActivityViewStateObservable().observe(this, this::render);
        viewModel.getOptionsMenuObservable().observe(this, this::render);

        fragmentAdapter = new FragmentAdapter(getSupportFragmentManager(), this);
        viewPager.setAdapter(fragmentAdapter);
        viewPager.setOffscreenPageLimit(2);
        viewPager.addOnPageChangeListener(this);
        tabLayout.setupWithViewPager(viewPager);

        currentViewPagerPosition = -1;

        onNewIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent.hasExtra(INTENT_EXTRA_STARTED_FROM_MAP_AVAILABLE_NOTIFICATION)) {
            MapDownloadService.cleanupMapAvailableNotification();
        }
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
    protected View getCoordinatorLayoutOrRootView() {
        //TODO: If I ever have a FAB in a fragment I will I have to ask my fragment for its coordinator layout?
        return coordinatorLayout;
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
        getMenuInflater().inflate(R.menu.activity_base, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        optionsMenu.updateAndroidMenu(menu, this);

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
        switch (item.getItemId()) {
            case R.id.nav_import:
            case R.id.nav_login_register:
            case R.id.nav_gps_status:
            case R.id.nav_settings:
                viewModel.onNavigationMenuItemSelected(navigationMenu.findItem(item.getItemId()));
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

        navigationMenu = viewState.navigationMenu;
        navigationMenu.updateAndroidMenu(navigationView.getMenu(), this);
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
        /*
           ViewPager calls FragmentManager.commitNowPermittingStateLoss() somewhere that causes an "IllegalState exception:
           FragmentManager is already executing transactions". This works around this situation.
        */
        if (!viewPagerInitialized) {
            viewPagerInitialized = true;

            viewPager.post(this::initViewPager);
        }
    }

    private void render(@Nullable StartActivityViewState state) {
        if (state == null) {
            return;
        }

        startActivity(state.getIntent(this));

        viewModel.onActivityStarted();
    }

    private void render(@Nullable MyMenu optionsMenu) {
        if (optionsMenu == null) {
            return;
        }

        if (optionsMenu != this.optionsMenu) {
            this.optionsMenu = optionsMenu;
            invalidateOptionsMenu();
        }
    }

    private void initViewPager() {
        fragmentAdapter.addTab(new FragmentAdapter.TabItem(R.string.track_list, true, MainActivityFragmentProvider.TRACK_LIST_FRAGMENT));
        fragmentAdapter.addTab(new FragmentAdapter.TabItem(R.string.map, true, MainActivityFragmentProvider.MAP_FRAGMENT));
        fragmentAdapter.addTab(new FragmentAdapter.TabItem(R.string.compass, true, MainActivityFragmentProvider.COMPASS_FRAGMENT));

        viewPager.getViewTreeObserver().addOnGlobalLayoutListener(this);
        viewPager.requestLayout();
        viewPager.setCurrentItem(currentViewPagerPosition);
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

        dialog.setListener(new PositiveNegativeButtonMessageDialog.Listener() {
            @Override
            public void onPositiveButtonClicked(@NonNull String tag, boolean neverAskAgain) {
                viewModel.onUserWantsToEnableGps(neverAskAgain);
            }

            @Override
            public void onNegativeButtonClicked(@NonNull String tag, boolean neverAskAgain) {
                viewModel.onUserDoesNotWantToEnableGps(neverAskAgain);
            }

            @Override
            public void onDialogCancelled(@NonNull String tag) {
                viewModel.onUserDoesNotWantToEnableGps(false);
            }
        });
    }

    @Override
    public void onFatalMessageDialogDismissed() {
        viewModel.onFatalErrorMessageDismissed();
    }

    private void showSnackbar(MessageWithTitle message) {
        StringBuilder sb = new StringBuilder();

        sb.append(getString(message.titleResId));

        String msg =  message.getMessage(this);

        if (!msg.isEmpty()) {
            sb.append("\n");
            sb.append(msg);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RequestCode.ENABLE_GPS:
                viewModel.onFinishedWaitingForGpsToBeEnabled();
                break;
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        /*
         * This is another hack. When there is a GLSurfaceView in the viewpager ofter when scrolling to other views from the GLSurfaceView
         * there will only be a black screen. I now set the MapView to INVISIBLE/VISIBLE in onVisibilityChanged.
         * This is here to early change its visibility to VISIBLE again when dragging the viewpager towards the MapFragment.
         */
        if ((position == 0 && positionOffset > 0) || (position == 1)) {
            fragmentAdapter.getFragment(position).onVisibilityChanged(true);
        }
    }

    @Override
    public void onPageSelected(int position) {
        if (currentViewPagerPosition == position) {
            return;
        }

        ViewPagerFragment frag;

        if (currentViewPagerPosition != -1) {
            frag = fragmentAdapter.getFragment(currentViewPagerPosition);

            if (frag != null) {
                frag.onVisibilityChanged(false);
            }
        }

        frag = fragmentAdapter.getFragment(position);

        if (frag != null) {
            currentViewPagerPosition = position;

            FirebaseAnalytics.getInstance(this)
                    .setCurrentScreen(this, frag.getClass().getSimpleName(), null);
        }

        viewPager.setPagingEnabled(position != 1);

        onVisibilityChanged(true);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    protected void onStop() {
        super.onStop();

        onVisibilityChanged(false);
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();

        onVisibilityChanged(true);
    }

    private void onVisibilityChanged(boolean visible) {
        ViewPagerFragment frag = fragmentAdapter.getFragment(viewPager.getCurrentItem());

        if (frag != null) {
            frag.onVisibilityChanged(visible);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(KEY_CURRENT_VIEWPAGER_POSITION, currentViewPagerPosition);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        currentViewPagerPosition = savedInstanceState.getInt(KEY_CURRENT_VIEWPAGER_POSITION);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onGlobalLayout() {

        if (Build.VERSION.SDK_INT < 16) {
            viewPager.getViewTreeObserver().removeGlobalOnLayoutListener(this);
        } else {
            viewPager.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        }

        /*
         * This is a hack. ViewPager does not call onPageSelected() when the initial fragment has been
         * displayed/selected
         */
        onPageSelected(viewPager.getCurrentItem());
    }
}
