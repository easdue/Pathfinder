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

package nl.erikduisters.pathfinder.ui.activity.main_activity;

import android.content.Context;

import dagger.Module;
import dagger.Provides;
import dagger.android.ContributesAndroidInjector;
import nl.erikduisters.pathfinder.di.ActivityContext;
import nl.erikduisters.pathfinder.di.FragmentScope;
import nl.erikduisters.pathfinder.ui.dialog.import_settings.ImportSettingsDialog;
import nl.erikduisters.pathfinder.ui.dialog.select_tracks_to_import.SelectTracksToImportDialog;
import nl.erikduisters.pathfinder.ui.fragment.compass.CompassFragment;
import nl.erikduisters.pathfinder.ui.fragment.init_storage.InitStorageFragment;
import nl.erikduisters.pathfinder.ui.fragment.map.MapFragment;
import nl.erikduisters.pathfinder.ui.fragment.play_services.PlayServicesFragment;
import nl.erikduisters.pathfinder.ui.fragment.runtime_permission.RuntimePermissionFragment;
import nl.erikduisters.pathfinder.ui.fragment.track_list.TrackListFragment;

/**
 * Created by Erik Duisters on 05-06-2018.
 */
@Module
public abstract class MainActivityBindingModule {
    @FragmentScope
    @ContributesAndroidInjector
    abstract InitStorageFragment contributeInitStorageFragmentInjector();

    @FragmentScope
    @ContributesAndroidInjector
    abstract RuntimePermissionFragment contributeRuntimePermissionFragmentInjector();

    @FragmentScope
    @ContributesAndroidInjector
    abstract PlayServicesFragment contributePlayServicesAvailabilityFragmentInjector();

    @FragmentScope
    @ContributesAndroidInjector
    abstract TrackListFragment contributeTrackListFragmentInjector();

    @FragmentScope
    @ContributesAndroidInjector
    abstract MapFragment contributeMapFragmentInjector();

    @FragmentScope
    @ContributesAndroidInjector
    abstract CompassFragment contributeCompassFragmentInjector();

    @FragmentScope
    @ContributesAndroidInjector
    abstract ImportSettingsDialog contributeImportSettingsDialog();

    @FragmentScope
    @ContributesAndroidInjector
    abstract SelectTracksToImportDialog contributeSelectTracksToImportDialog();

    @Provides
    @ActivityContext
    static Context provideActivityContext(MainActivity activity) {
        return activity;
    }
}
