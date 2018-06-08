package nl.erikduisters.pathfinder.ui;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import dagger.android.AndroidInjection;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.support.HasSupportFragmentInjector;
import nl.erikduisters.pathfinder.viewmodel.ViewModelFactory;

/**
 * Created by Erik Duisters on 02-06-2018.
 */
public abstract class BaseActivity<VM extends ViewModel> extends AppCompatActivity implements HasSupportFragmentInjector {
    @Inject
    DispatchingAndroidInjector<Fragment> dispatchingAndroidInjector;


    @Inject
    ViewModelFactory viewModelFactory;

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
    protected void onResumeFragments() {
        super.onResumeFragments();

        isFragmentStateLocked = false;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        isFragmentStateLocked = true;
    }

    @Override
    protected void onDestroy() {
        if (unbinder != null) {
            unbinder.unbind();
        }

        super.onDestroy();
    }

    protected <T extends Fragment> T findFragment(String tag) {
        //noinspection unchecked
        return (T) getSupportFragmentManager().findFragmentByTag(tag);
    }

    protected void addFragment(Fragment fragment, String tag) {
        getSupportFragmentManager()
                .beginTransaction()
                .add(fragment, tag)
                .commit();
    }

    protected void addFragment(@IdRes int containerViewId, Fragment fragment, String tag) {
        getSupportFragmentManager()
                .beginTransaction()
                .add(containerViewId, fragment, tag)
                .commit();
    }

    protected void removeFragment(String tag) {
        Fragment fragment = getSupportFragmentManager()
                .findFragmentByTag(tag);

        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .remove(fragment)
                    .commit();
        }
    }

    protected void show(DialogFragment dialog, String tag) {
        getSupportFragmentManager()
                .beginTransaction()
                .add(dialog, tag)
                .commit();
    }

    public void dismissDialogFragment(String tag) {
        DialogFragment fragment = (DialogFragment) getSupportFragmentManager().findFragmentByTag(tag);

        if (fragment != null) {
            fragment.dismiss();
        }
    }
}
