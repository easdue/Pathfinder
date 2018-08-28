/*
 * Copyright (c) 2018 Erik Duisters
 *
 * This file is part of Pathfinder
 *
 * Pathfinder is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Foobar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Pathfinder. If not, see <https://www.gnu.org/licenses/>.
 */

package nl.erikduisters.pathfinder.ui;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
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
public abstract class BaseFragment<VM extends ViewModel> extends Fragment implements BackPressed {
    @Nullable private Unbinder unbinder;

    @Inject
    ViewModelFactory viewModelFactory;

    protected VM viewModel;

    @Override
    public void onAttach(Context context) {
        Timber.d("%s.onAttach()", getClass().getSimpleName());

        AndroidSupportInjection.inject(this);
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(getViewModelClass());

        super.onAttach(context);
    }

    @Nullable
    @Override
    @CallSuper
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Timber.d("%s.onCreateView()", getClass().getSimpleName());
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
        Timber.d("%s.onDestroyView()", getClass().getSimpleName());
        if (unbinder != null) {
            unbinder.unbind();
        }

        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        Timber.d("%s.onDestroy()", getClass().getSimpleName());
        super.onDestroy();
    }

    protected void invalidateOptionsMenu() {
        FragmentActivity activity = getActivity();

        if (activity != null) {
            activity.invalidateOptionsMenu();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Timber.d("%s.onCreate(savedInstanceState = %s", getClass().getSimpleName(), savedInstanceState == null ? "null" : "not null");
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        Timber.d("%s.onStart()", getClass().getSimpleName());
        super.onStart();
    }

    @Override
    public void onResume() {
        Timber.d("%s.onResume()", getClass().getSimpleName());
        super.onResume();

        /* On API 19 I eventually get crashed due to a "Too many open files" exception and LeakCanary detects leaks
        if (!(this instanceof ViewPagerFragment) && !(this instanceof HeadlessFragment)) {
            FirebaseAnalytics.getInstance(requireContext())
                    .setCurrentScreen(requireActivity(), this.getClass().getSimpleName(), null);
        }
        */
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        Timber.d("%s.onSaveInstanceState()", getClass().getSimpleName());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        Timber.d("%s.onPause()", getClass().getSimpleName());
        super.onPause();
    }

    @Override
    public void onStop() {
        Timber.d("%s.onStop()", getClass().getSimpleName());
        super.onStop();
    }

    @Override
    public void onDetach() {
        Timber.d("%s.onDetach()", getClass().getSimpleName());
        super.onDetach();
    }

    protected <T extends Fragment> T findFragment(String tag) {
        //noinspection unchecked
        return (T) getChildFragmentManager().findFragmentByTag(tag);
    }

    protected void show(DialogFragment fragment, String tag) {
        Timber.d("Showing dialog: %s, tag: %s", fragment.getClass().getSimpleName(), tag);
        getChildFragmentManager()
                .beginTransaction()
                .add(fragment, tag)
                .commit();
    }

    protected void dismissDialogFragment(String tag) {
        DialogFragment fragment = (DialogFragment) getChildFragmentManager().findFragmentByTag(tag);

        if (fragment != null) {
            Timber.d("Dismissing dialog: %s, tag: %s", fragment.getClass().getSimpleName(), tag);
            fragment.dismiss();
        }
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }
}
