package nl.erikduisters.pathfinder.ui;

import android.arch.lifecycle.ViewModelProviders;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.lang.reflect.Field;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import dagger.android.AndroidInjection;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.support.HasSupportFragmentInjector;
import nl.erikduisters.pathfinder.R;
import nl.erikduisters.pathfinder.data.local.PreferenceManager;
import nl.erikduisters.pathfinder.service.MapDownloadService;
import nl.erikduisters.pathfinder.ui.BaseActivityViewState.DisplayMessageState;
import nl.erikduisters.pathfinder.ui.BaseActivityViewState.DisplayMessageState.DisplayDuration;
import nl.erikduisters.pathfinder.ui.BaseActivityViewState.RetryRetryableMapDownloadsState;
import nl.erikduisters.pathfinder.ui.BaseActivityViewState.SetOptionsMenuState;
import nl.erikduisters.pathfinder.ui.BaseActivityViewState.ShowPositiveNegativeDialogState;
import nl.erikduisters.pathfinder.ui.BaseActivityViewState.StartActivityState;
import nl.erikduisters.pathfinder.ui.dialog.PositiveNegativeButtonMessageDialog;
import nl.erikduisters.pathfinder.util.menu.MyMenu;
import nl.erikduisters.pathfinder.util.menu.MyMenuItem;
import nl.erikduisters.pathfinder.viewmodel.ViewModelFactory;
import timber.log.Timber;

/**
 * Created by Erik Duisters on 02-06-2018.
 */

public abstract class BaseActivity<VM extends BaseActivityViewModel>
        extends AppCompatActivity
        implements HasSupportFragmentInjector, MapDownloadService.Listener {
    private static final String TAG_POSITIVE_NEGATIVE_BUTTON_DIALOG = "PositiveNegativeButtonDialog";

    @Inject
    DispatchingAndroidInjector<Fragment> dispatchingAndroidInjector;

    @Inject
    protected ViewModelFactory viewModelFactory;
    @Inject
    PreferenceManager preferenceManager;

    protected VM viewModel;

    private boolean isFragmentStateLocked;
    protected MapDownloadServiceConnection mapDownloadServiceConnection;
    private MapDownloadService mapDownloadService;
    private @NonNull MyMenu optionsMenu;

    @Nullable
    private Unbinder unbinder;

    public BaseActivity() {
        optionsMenu = new MyMenu();
    }

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);

        Timber.d("%s.onCreate()", getClass().getSimpleName());

        setContentView(getLayoutResId());

        unbinder = ButterKnife.bind(this);

        isFragmentStateLocked = true;

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(getViewModelClass());
        mapDownloadServiceConnection = new MapDownloadServiceConnection();

        viewModel.getBaseActivityViewStateObservable().observe(this, this::render);
    }

    protected abstract @LayoutRes int getLayoutResId();
    protected abstract Class<VM> getViewModelClass();
    protected abstract View getCoordinatorLayoutOrRootView();

    @Override
    public DispatchingAndroidInjector<Fragment> supportFragmentInjector() {
        return dispatchingAndroidInjector;
    }

    @Override
    protected void onPostResume() {
        Timber.d("%s.onPostResume()", getClass().getSimpleName());
        super.onPostResume();
    }

    @Override
    protected void onStart() {
        Timber.d("%s.onStart()", getClass().getSimpleName());
        super.onStart();

        bindMapDownloadService();
    }

    @Override
    protected void onStop() {
        Timber.d("%s.onStop()", getClass().getSimpleName());
        super.onStop();

        // If I want the MapDownloadService to keep running move the remainder to onDestroy();
        unbindMapDownloadService();
    }

    @Override
    protected void onPause() {
        Timber.d("%s.onPause()", getClass().getSimpleName());
        super.onPause();
    }

    @Override
    protected void onResume() {
        Timber.d("%s.onResume()", getClass().getSimpleName());
        super.onResume();
    }

    @Override
    protected void onResumeFragments() {
        Timber.d("%s.onResumeFragments()", getClass().getSimpleName());
        super.onResumeFragments();

        isFragmentStateLocked = false;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Timber.d("%s.onSaveInstanceState()", getClass().getSimpleName());
        super.onSaveInstanceState(outState);

        isFragmentStateLocked = true;
    }

    @Override
    protected void onDestroy() {
        Timber.d("%s.onDestroy()", getClass().getSimpleName());
        if (unbinder != null) {
            unbinder.unbind();
        }

        fixInputMethod(this);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

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
        switch (item.getItemId()) {
            case R.id.menu_failed_downloads:
            case R.id.menu_retryable_downloads:
                MyMenuItem menuItem = optionsMenu.findItem(item.getItemId());
                viewModel.onOptionsItemSelected(menuItem);
                return true;
            default:
                return false;
        }
    }

    //Fix for InputMethodManager leaks the last focused view, see https://issuetracker.google.com/issues/37043700
    private void fixInputMethod(Context context) {
        if (context == null) {
            return;
        }
        InputMethodManager inputMethodManager = null;
        try {
            inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        } catch (Throwable th) {
            th.printStackTrace();
        }
        if (inputMethodManager == null) {
            return;
        }
        Field[] declaredFields = inputMethodManager.getClass().getDeclaredFields();
        for (Field declaredField : declaredFields) {
            try {
                if (!declaredField.isAccessible()) {
                    declaredField.setAccessible(true);
                }
                Object obj = declaredField.get(inputMethodManager);
                if (obj == null || !(obj instanceof View)) {
                    continue;
                }
                View view = (View) obj;
                if (view.getContext() == context) {
                    declaredField.set(inputMethodManager, null);
                } else {
                    return;
                }
            } catch (Throwable th) {
                th.printStackTrace();
            }
        }
    }

    protected <T extends Fragment> T findFragment(String tag) {
        //noinspection unchecked
        return (T) getSupportFragmentManager().findFragmentByTag(tag);
    }

    protected <T extends Fragment> T findFragment(int id) {
        //noinspection unchecked
        return (T) getSupportFragmentManager().findFragmentById(id);
    }

    protected void addFragment(Fragment fragment, String tag) {
        Timber.d("Adding fragment: %s, tag: %s", fragment.getClass().getSimpleName(), tag);
        getSupportFragmentManager()
                .beginTransaction()
                .add(fragment, tag)
                .commit();
    }

    protected void addFragment(@IdRes int containerViewId, Fragment fragment, String tag) {
        Timber.d("Adding fragment: %s, tag: %s", fragment.getClass().getSimpleName(), tag);
        getSupportFragmentManager()
                .beginTransaction()
                .add(containerViewId, fragment, tag)
                .commit();
    }

    protected void removeFragment(String tag) {
        Fragment fragment = getSupportFragmentManager()
                .findFragmentByTag(tag);

        if (fragment != null) {
            Timber.d("Removing fragment: %s, tag: %s", fragment.getClass().getSimpleName(), tag);
            getSupportFragmentManager()
                    .beginTransaction()
                    .remove(fragment)
                    .commit();
        }
    }

    protected void show(DialogFragment dialog, String tag) {
        Timber.d("Showing dialog: %s, tag: %s", dialog.getClass().getSimpleName(), tag);
        getSupportFragmentManager()
                .beginTransaction()
                .add(dialog, tag)
                .commit();
    }

    public void dismissDialogFragment(String tag) {
        DialogFragment fragment = (DialogFragment) getSupportFragmentManager().findFragmentByTag(tag);

        if (fragment != null) {
            Timber.d("Dismissing dialog: %s, tag: %s", fragment.getClass().getSimpleName(), tag);
            fragment.dismiss();
        }
    }

    protected void bindMapDownloadService() {
        if (preferenceManager.areMapsDownloading()) {
            Intent intent = new Intent(this, MapDownloadService.class);
            bindService(intent, mapDownloadServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    private void unbindMapDownloadService() {
        if (mapDownloadService != null) {
            mapDownloadService.removeListener(this);

            unbindService(mapDownloadServiceConnection);
            mapDownloadService = null;
        }
    }

    private class MapDownloadServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Timber.e("onServiceConnected");

            mapDownloadService = ((MapDownloadService.MapDownloadServiceBinder)service).getService();

            if (viewModel.getBaseActivityViewStateObservable().getValue() instanceof RetryRetryableMapDownloadsState) {
                mapDownloadService.retryRetryableMapDownloads();

                viewModel.onRetryingRetryableMapDownloads();
            }

            mapDownloadService.addListener(BaseActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Timber.e("onServiceDisconnected");
            mapDownloadService = null;
        }
    }

    @Override
    public void onMapAvailable(String mapName) {
        viewModel.onMapAvailable(mapName);
    }

    @Override
    public void onMapDownloadFailed(String zipFileName) {
        viewModel.onMapDownloadFailed(zipFileName);
    }

    @Override
    public void onFailedMapDownloadsAvailable(int numFailedDownloads) {
        viewModel.onFailedMapDownloadsAvailable(numFailedDownloads);
    }

    @Override
    public void onRetryableMapDownloadsAvailable(int numRetryableDownloads) {
        viewModel.onRetryableMapDownloadsAvailable(numRetryableDownloads);
    }

    @Override
    public void onMapUnzipFailed(String fileName) {
        viewModel.onMapUnzipFailed(fileName);
    }

    @Override
    public void onMapDownloadComplete() {
        unbindMapDownloadService();
    }

    private void render(@Nullable BaseActivityViewState viewState) {
        if (viewState == null) {
            return;
        }

        if (viewState instanceof SetOptionsMenuState) {
            render((SetOptionsMenuState) viewState);
        }

        if (viewState instanceof DisplayMessageState) {
            render((DisplayMessageState) viewState);
        }

        if (viewState instanceof StartActivityState) {
            render((StartActivityState) viewState);
        }

        if (viewState instanceof ShowPositiveNegativeDialogState) {
            showPositiveNegativeButtonDialog((ShowPositiveNegativeDialogState) viewState, TAG_POSITIVE_NEGATIVE_BUTTON_DIALOG);
        } else {
            dismissDialogFragment(TAG_POSITIVE_NEGATIVE_BUTTON_DIALOG);
        }

        if (viewState instanceof RetryRetryableMapDownloadsState) {
            render((RetryRetryableMapDownloadsState) viewState);
        }
    }

    private void render(SetOptionsMenuState state) {
        if (state.optionsMenu != optionsMenu) {
            optionsMenu = state.optionsMenu;
            invalidateOptionsMenu();
        }
    }

    private void render(DisplayMessageState state) {
        Snackbar.make(getCoordinatorLayoutOrRootView(), state.getMessage(this),
                displayDuration2SnackbarLength(state.displayDuration))
                .show();

        viewModel.onMessageDisplayed();
    }

    private void render(StartActivityState state) {
        startActivity(state.getIntent());

        viewModel.onActivityStarted();
    }

    private void render(RetryRetryableMapDownloadsState state) {
        if (mapDownloadService == null) {
            bindMapDownloadService();
        } else {
            mapDownloadService.retryRetryableMapDownloads();
            viewModel.onRetryingRetryableMapDownloads();
        }
    }

    private int displayDuration2SnackbarLength(@DisplayDuration int duration) {
        switch (duration) {
            case DisplayDuration.SHORT:
                return Snackbar.LENGTH_SHORT;
            case DisplayDuration.LONG:
                return Snackbar.LENGTH_LONG;
            default:
                throw new IllegalStateException("Unhandled duration");
        }
    }

    private void showPositiveNegativeButtonDialog(ShowPositiveNegativeDialogState state, @NonNull String tag) {
        PositiveNegativeButtonMessageDialog dialog = findFragment(tag);

        if (dialog == null) {
            dialog = PositiveNegativeButtonMessageDialog
                    .newInstance(state.dialogInfo);

            dialog.show(getSupportFragmentManager(), tag);
        }

        dialog.setListener(new PositiveNegativeButtonMessageDialog.Listener() {
            @Override
            public void onPositiveButtonClicked(boolean neverAskAgain) {
                viewModel.onPositiveButtonClicked(neverAskAgain);
            }

            @Override
            public void onNegativeButtonClicked(boolean neverAskAgain) {
                viewModel.onNegativeButtonClicked(neverAskAgain);
            }

            @Override
            public void onDialogCancelled() {
                viewModel.onDialogCancelled();
            }
        });
    }
}
