package nl.erikduisters.pathfinder.ui.activity.main_activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
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

import java.util.List;

import butterknife.BindView;
import de.hdodenhof.circleimageview.CircleImageView;
import nl.erikduisters.pathfinder.R;
import nl.erikduisters.pathfinder.service.MapDownloadService;
import nl.erikduisters.pathfinder.service.gpsies_service.SearchTracks;
import nl.erikduisters.pathfinder.service.track_import.ImportJob;
import nl.erikduisters.pathfinder.service.track_import.TrackImportService;
import nl.erikduisters.pathfinder.ui.BaseActivity;
import nl.erikduisters.pathfinder.ui.RequestCode;
import nl.erikduisters.pathfinder.ui.activity.FragmentAdapter;
import nl.erikduisters.pathfinder.ui.activity.main_activity.MainActivityViewState.AskUserToEnableGpsState;
import nl.erikduisters.pathfinder.ui.activity.main_activity.MainActivityViewState.CheckPlayServicesAvailabilityState;
import nl.erikduisters.pathfinder.ui.activity.main_activity.MainActivityViewState.InitDatabaseState;
import nl.erikduisters.pathfinder.ui.activity.main_activity.MainActivityViewState.InitStorageViewState;
import nl.erikduisters.pathfinder.ui.activity.main_activity.MainActivityViewState.InitializedState;
import nl.erikduisters.pathfinder.ui.activity.main_activity.MainActivityViewState.InitializedState.ShowDialogViewState;
import nl.erikduisters.pathfinder.ui.activity.main_activity.MainActivityViewState.InitializedState.ShowDialogViewState.SelectTracksToImportDialogState;
import nl.erikduisters.pathfinder.ui.activity.main_activity.MainActivityViewState.RequestRuntimePermissionState;
import nl.erikduisters.pathfinder.ui.activity.main_activity.MainActivityViewState.ShowEnableGpsSettingState;
import nl.erikduisters.pathfinder.ui.activity.main_activity.MainActivityViewState.ShowFatalErrorMessageState;
import nl.erikduisters.pathfinder.ui.activity.main_activity.MainActivityViewState.WaitingForGpsToBeEnabledState;
import nl.erikduisters.pathfinder.ui.dialog.FatalMessageDialog;
import nl.erikduisters.pathfinder.ui.dialog.MessageWithTitle;
import nl.erikduisters.pathfinder.ui.dialog.PositiveNegativeButtonMessageDialog;
import nl.erikduisters.pathfinder.ui.dialog.ProgressDialog;
import nl.erikduisters.pathfinder.ui.dialog.import_settings.ImportSettingsDialog;
import nl.erikduisters.pathfinder.ui.dialog.select_tracks_to_import.SelectTracksToImportDialog;
import nl.erikduisters.pathfinder.ui.fragment.ViewPagerFragment;
import nl.erikduisters.pathfinder.ui.fragment.init_storage.InitStorageFragment;
import nl.erikduisters.pathfinder.ui.fragment.map.MapFragment;
import nl.erikduisters.pathfinder.ui.fragment.play_services.PlayServicesFragment;
import nl.erikduisters.pathfinder.ui.fragment.runtime_permission.RuntimePermissionFragment;
import nl.erikduisters.pathfinder.ui.fragment.runtime_permission.RuntimePermissionFragmentViewModel;
import nl.erikduisters.pathfinder.ui.view.MyViewPager;
import nl.erikduisters.pathfinder.util.menu.MyMenu;
import timber.log.Timber;

//TODO: Remove fragment listeners
//TODO: Bottom navigation instead of tabs?
//TODO: Remind users to install an offline map
//TODO: All dialogs must be dismissible using the back button
//TODO: Add "Download offline map" to navigation menu
public class MainActivity
        extends BaseActivity<MainActivityViewModel>
        implements NavigationView.OnNavigationItemSelectedListener, InitStorageFragment.InitStorageFragmentListener,
                   RuntimePermissionFragment.RuntimePermissionFragmentListener,
                   PlayServicesFragment.PlayServicesFragmentListener,
                   ViewPager.OnPageChangeListener, ViewTreeObserver.OnGlobalLayoutListener,
        TrackImportScheduler {

    private static final String TAG_INIT_STORAGE_FRAGMENT = "InitStorageFragment";
    private static final String TAG_RUNTIME_PERMISSION_FRAGMENT = "RuntimePermissionFragment";
    private static final String TAG_PLAY_SERVICES_AVAILABIITY_FRAGMENT = "PlayServicesAvailablilityFragment";
    private static final String TAG_FATAL_MESSAGE_DIALOG = "FatalMessageDialog";
    private static final String TAG_INIT_DATABASE_PROGRESS_DIALOG = "InitDatabaseProgressDialog";
    private static final String TAG_ASK_USER_TO_ENABLE_GPS_DIALOG = "AskUserToEnableGpsDialog";
    private static final String TAG_IMPORT_SETTINGS_DIALOG = "ImportSettingsDialog";
    private static final String TAG_SELECT_TRACKS_TO_IMPORT_DIALOG = "SelectTrackToImportDialog";

    private static final String KEY_CURRENT_VIEWPAGER_POSITION = "CurrentViewpagerPosition";
    private static final String KEY_VIEW_MODEL_STATE = "ViewModelState";

    public static final String INTENT_EXTRA_STARTED_FROM_MAP_AVAILABLE_NOTIFICATION = "nl.erikduisters.pathfinder.StartedFromMapAvailableNotification";
    public static final String INTENT_EXTRA_FAILED_TRACK_IMPORT_JOB_INFO = "nl.erikduisters.pathfinder.FailedTrackImportJobInfo";
    public static final String INTENT_EXTRA_FAILED_TRACK_IMPORT_REASON = "nl.erikduisters.pathfinder.FailedTrackImportReason";

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

        currentViewPagerPosition = -1;

        if (savedInstanceState != null) {
            currentViewPagerPosition = savedInstanceState.getInt(KEY_CURRENT_VIEWPAGER_POSITION);
            viewModel.onRestoreInstanceState(savedInstanceState.getParcelable(KEY_VIEW_MODEL_STATE));
        }

        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        View headerView = navigationView.getHeaderView(0);
        avatar = headerView.findViewById(R.id.gpsies_avatar);
        username = headerView.findViewById(R.id.gpsies_username);

        fragmentAdapter = new FragmentAdapter(getSupportFragmentManager(), this);
        viewPager.setAdapter(fragmentAdapter);
        viewPager.setOffscreenPageLimit(2);
        viewPager.addOnPageChangeListener(this);
        tabLayout.setupWithViewPager(viewPager);

        viewModel.getMainActivityViewStateObservable().observe(this, this::render);
        viewModel.getStartActivityViewStateObservable().observe(this, this::render);

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
        //TODO: If I ever have a FAB in a fragment will I have to ask my fragment for its coordinator layout?
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

        if (viewState instanceof ShowFatalErrorMessageState) {
            ShowFatalErrorMessageState state = (ShowFatalErrorMessageState) viewState;
            showFatalMessageDialog(TAG_FATAL_MESSAGE_DIALOG, state.message, state.finishOnDismiss);
        } else {
            dismissDialogFragment(TAG_FATAL_MESSAGE_DIALOG);
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

        render(state.navigationViewState);
        render(state.optionsMenu);
        render(state.showDialogViewState);
    }

    void render(@NonNull InitializedState.NavigationViewState viewState) {
        avatar.setImageDrawable(viewState.avatar.getDrawable(this));
        username.setText(viewState.userName.getString(this));

        if (navigationMenu != viewState.navigationMenu) {
            navigationMenu = viewState.navigationMenu;
            navigationMenu.updateAndroidMenu(navigationView.getMenu(), this);
        }
    }

    private void render(@NonNull MyMenu optionsMenu) {
        if (optionsMenu != this.optionsMenu) {
            this.optionsMenu = optionsMenu;
            invalidateOptionsMenu();
        }
    }

    private void render(@Nullable MainActivityViewState.StartActivityViewState state) {
        if (state == null) {
            return;
        }

        startActivity(state.getIntent(this));

        viewModel.onActivityStarted();
    }

    private void render(@Nullable ShowDialogViewState viewState) {
        if (viewState instanceof ShowDialogViewState.ShowImportSettingsDialogState) {
            showImportSettingsDialog(TAG_IMPORT_SETTINGS_DIALOG);
        } else {
            dismissDialogFragment(TAG_IMPORT_SETTINGS_DIALOG);
        }

        if (viewState instanceof SelectTracksToImportDialogState) {
            showSelectTracksToImportDialog((SelectTracksToImportDialogState) viewState, TAG_SELECT_TRACKS_TO_IMPORT_DIALOG);
        } else {
            dismissDialogFragment(TAG_SELECT_TRACKS_TO_IMPORT_DIALOG);
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
    public void onStorageInitializationFailed() {
        viewModel.onStorageInitializationFailed();
    }

    private void showFatalMessageDialog(String tag, MessageWithTitle message, boolean finishOnDismiss) {
        FatalMessageDialog dialog = findFragment(tag);

        if (dialog == null) {
            dialog = FatalMessageDialog.newInstance(message);

            show(dialog, tag);
        }

        dialog.setListener(() -> {
            if (finishOnDismiss) {
                finish();
            }

            viewModel.onFatalErrorMessageDismissed();
        });
    }

    private void showAskUserToEnableGpsDialog(AskUserToEnableGpsState state, String tag) {
        PositiveNegativeButtonMessageDialog dialog = findFragment(tag);

        if (dialog == null) {
            dialog = PositiveNegativeButtonMessageDialog.newInstance(state.dialogInfo);

            show(dialog, tag);
        }

        dialog.setListener(new PositiveNegativeButtonMessageDialog.Listener() {
            @Override
            public void onPositiveButtonClicked(boolean neverAskAgain) {
                viewModel.onUserWantsToEnableGps(neverAskAgain);
            }

            @Override
            public void onNegativeButtonClicked(boolean neverAskAgain) {
                viewModel.onUserDoesNotWantToEnableGps(neverAskAgain);
            }

            @Override
            public void onDialogCancelled() {
                viewModel.onUserDoesNotWantToEnableGps(false);
            }
        });
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

    private void showImportSettingsDialog(String tag) {
        ImportSettingsDialog dialog = findFragment(tag);

        if (dialog == null) {
            MapFragment mapFragment = fragmentAdapter.getFragment(MainActivityFragmentProvider.MAP_FRAGMENT);

            if (mapFragment == null) {
                throw new IllegalStateException("Cannot show ImportSettingsDialog when MapFragment is not around yet");
            }

            mapFragment.onSaveInstanceState();  //Make sure map bounding box is available in shared preferences

            dialog = ImportSettingsDialog.newInstance();
            show(dialog, tag);
        }

        dialog.setListener(new ImportSettingsDialog.Listener() {
            @Override
            public void onImportSettingsDialogDismissed(SearchTracks.JobInfo jobInfo) {
                viewModel.onImportSettingsDialogDismissed(jobInfo);
            }

            /* TODO
            @Override
            public void onImportSettingsDialogDismissed(TrackImportService.Job) {
                viewModel.onImportSettingsDialogDismissed(xx);
            }
            */

            @Override
            public void onImportSettingsDialogCancelled() {
                viewModel.onImportSettingsDialogCancelled();
            }
        });
    }

    private void showSelectTracksToImportDialog(SelectTracksToImportDialogState viewState, String tag) {
        SelectTracksToImportDialog dialog = findFragment(tag);

        if (dialog == null) {
            dialog = SelectTracksToImportDialog.newInstance(viewState.jobInfo);

            show(dialog, tag);
        }

        dialog.setListener(new SelectTracksToImportDialog.Listener() {
            @Override
            public void onSelectTracksToImportDialogDismissed(List<String> trackFileIds) {
                viewModel.onSelectTracksToImportDialogDismissed(trackFileIds, MainActivity.this);
            }

            @Override
            public void onSelectTracksToImportDialogCancelled() {
                viewModel.onSelectTracksToImportDialogCancelled();
            }
        });
    }

    @Override
    public void scheduleTrackDownload(ImportJob.JobInfo jobInfo) {
        Intent intent = new Intent(TrackImportService.ACTION_IMPORT_TRACKS);
        intent.putExtra(TrackImportService.EXTRA_IMPORT_TRACKS_JOB_INFO, jobInfo);

        TrackImportService.enqueueWork(this, intent);
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

            /*TODO: I disabled analytics because
            FirebaseAnalytics.getInstance(this)
                    .setCurrentScreen(this, frag.getClass().getSimpleName(), null);
            */
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
        outState.putParcelable(KEY_VIEW_MODEL_STATE, viewModel.onSaveInstanceState());
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
