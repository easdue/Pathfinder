package nl.erikduisters.pathfinder.ui;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.lang.reflect.Field;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import dagger.android.AndroidInjection;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.support.HasSupportFragmentInjector;
import nl.erikduisters.pathfinder.viewmodel.ViewModelFactory;
import timber.log.Timber;

/**
 * Created by Erik Duisters on 02-06-2018.
 */
public abstract class BaseActivity<VM extends ViewModel> extends AppCompatActivity implements HasSupportFragmentInjector {
    @Inject
    DispatchingAndroidInjector<Fragment> dispatchingAndroidInjector;


    @Inject
    protected ViewModelFactory viewModelFactory;

    protected VM viewModel;

    private boolean isFragmentStateLocked;

    @Nullable
    private Unbinder unbinder;

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);

        setContentView(getLayoutResId());

        unbinder = ButterKnife.bind(this);

        isFragmentStateLocked = true;

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(getViewModelClass());
    }

    protected abstract @LayoutRes int getLayoutResId();
    protected abstract Class<VM> getViewModelClass();

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
    }

    @Override
    protected void onStop() {
        Timber.d("%s.onStop()", getClass().getSimpleName());
        super.onStop();
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
}
