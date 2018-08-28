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

package nl.erikduisters.pathfinder.ui.activity.settings;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.FragmentManager;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import butterknife.BindView;
import nl.erikduisters.pathfinder.R;
import nl.erikduisters.pathfinder.ui.BaseActivity;
import nl.erikduisters.pathfinder.ui.fragment.settings.SettingsFragment;
import nl.erikduisters.pathfinder.viewmodel.VoidViewModel;

/**
 * Created by Erik Duisters on 18-07-2018.
 */

public class SettingsActivity
        extends BaseActivity<VoidViewModel>
        implements PreferenceFragmentCompat.OnPreferenceStartFragmentCallback,
                   PreferenceFragmentCompat.OnPreferenceStartScreenCallback {

    @BindView(R.id.contraintLayout) ConstraintLayout constraintLayout;
    @BindView(R.id.toolbar) Toolbar toolbar;
    private SettingsFragment settingsFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        settingsFragment = (SettingsFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentPlaceHolder);

        if (settingsFragment == null) {
            settingsFragment = SettingsFragment.newInstance(null);

            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragmentPlaceHolder, settingsFragment)
                    .commit();
        }
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_settings;
    }

    @Override
    protected Class<VoidViewModel> getViewModelClass() {
        return VoidViewModel.class;
    }

    @Override
    protected View getCoordinatorLayoutOrRootView() {
        return constraintLayout;
    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference pref) {
        //TODO: Do I ever need this?
        return false;
    }

    @Override
    public boolean onPreferenceStartScreen(PreferenceFragmentCompat caller, PreferenceScreen pref) {
        SettingsFragment settingsFragment = SettingsFragment.newInstance(pref.getKey());
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentPlaceHolder, settingsFragment)
                .addToBackStack(null)
                .commit();

        getSupportActionBar().setTitle(pref.getTitle());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                FragmentManager fm = getSupportFragmentManager();

                if (fm.getBackStackEntryCount() > 0) {
                    fm.popBackStack();
                    return true;
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void setToolbarTitle(CharSequence title) {
        getSupportActionBar().setTitle(title);
    }

    @Override
    public void onMapAvailable(String mapName) {
        super.onMapAvailable(mapName);

        settingsFragment.onMapAvailable();
    }
}
