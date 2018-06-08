package nl.erikduisters.pathfinder.ui;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import dagger.android.support.AndroidSupportInjection;
import nl.erikduisters.pathfinder.viewmodel.ViewModelFactory;
import timber.log.Timber;

/**
 * Created by Erik Duisters on 02-06-2018.
 */

public abstract class BaseFragment<VM extends ViewModel> extends Fragment {
    @Nullable private Unbinder unbinder;

    @Inject
    ViewModelFactory viewModelFactory;

    protected VM viewModel;

    @Override
    public void onAttach(Context context) {
        Timber.e("onAttach(Context)");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            AndroidSupportInjection.inject(this);
            viewModel = ViewModelProviders.of(this, viewModelFactory).get(getViewModelClass());
        }

        super.onAttach(context);
    }

    @Nullable
    @Override
    @CallSuper
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Timber.e("onCreateView()");
        int layoutResId = getLayoutResId();

        if (layoutResId > 0) {
            View v = inflater.inflate(getLayoutResId(), container, false);

            unbinder = ButterKnife.bind(this, v);

            return v;
        }

        return null;
    }

    protected abstract @LayoutRes int getLayoutResId();
    protected abstract Class<VM> getViewModelClass();

    @Override
    public void onDestroyView() {
        Timber.e("onDestroyView()");
        if (unbinder != null) {
            unbinder.unbind();
        }

        super.onDestroyView();
    }

    protected void invalidateOptionsMenu() {
        FragmentActivity activity = getActivity();

        if (activity != null) {
            activity.invalidateOptionsMenu();
        }
    }

    protected <T extends Fragment> T findFragment(String tag) {
        //noinspection unchecked
        return (T) getChildFragmentManager().findFragmentByTag(tag);
    }

    protected void show(DialogFragment fragment, String tag) {
        getChildFragmentManager()
                .beginTransaction()
                .add(fragment, tag)
                .commit();
    }

    protected void dismissDialogFragment(String tag) {
        DialogFragment fragment = (DialogFragment) getChildFragmentManager().findFragmentByTag(tag);

        if (fragment != null) {
            fragment.dismiss();
        }
    }
}
