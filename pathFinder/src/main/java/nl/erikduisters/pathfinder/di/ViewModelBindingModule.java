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

package nl.erikduisters.pathfinder.di;

import android.arch.lifecycle.ViewModel;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;
import nl.erikduisters.pathfinder.ui.activity.gps_status.GpsStatusActivityViewModel;
import nl.erikduisters.pathfinder.ui.activity.main_activity.MainActivityViewModel;
import nl.erikduisters.pathfinder.ui.activity.map_download.MapDownloadActivityViewModel;
import nl.erikduisters.pathfinder.ui.dialog.import_settings.ImportSettingsDialogViewModel;
import nl.erikduisters.pathfinder.ui.dialog.select_tracks_to_import.SelectTracksToImportDialogViewModel;
import nl.erikduisters.pathfinder.ui.fragment.compass.CompassFragmentViewModel;
import nl.erikduisters.pathfinder.ui.fragment.gps_status.GpsStatusFragmentViewModel;
import nl.erikduisters.pathfinder.ui.fragment.init_storage.InitStorageFragmentViewModel;
import nl.erikduisters.pathfinder.ui.fragment.map.MapFragmentViewModel;
import nl.erikduisters.pathfinder.ui.fragment.map_download.MapDownloadFragmentViewModel;
import nl.erikduisters.pathfinder.ui.fragment.play_services.PlayServicesFragmentViewModel;
import nl.erikduisters.pathfinder.ui.fragment.runtime_permission.RuntimePermissionFragmentViewModel;
import nl.erikduisters.pathfinder.ui.fragment.track_list.TrackListFragmentViewModel;
import nl.erikduisters.pathfinder.viewmodel.VoidViewModel;

/**
 * Created by Erik Duisters on 01-06-2018.
 */

@Module
abstract class ViewModelBindingModule {
    @Binds
    @IntoMap
    @ViewModelKey(VoidViewModel.class)
    abstract ViewModel bindVoidViewModel(VoidViewModel voidViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(MainActivityViewModel.class)
    abstract ViewModel bindMainActivityViewModel(MainActivityViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(InitStorageFragmentViewModel.class)
    abstract ViewModel bindInitStorageFragmentVieModel(InitStorageFragmentViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(RuntimePermissionFragmentViewModel.class)
    abstract ViewModel bindRuntimePermissionFragmentViewModel(RuntimePermissionFragmentViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(PlayServicesFragmentViewModel.class)
    abstract ViewModel bindPlayServicesFragmentViewModel(PlayServicesFragmentViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(TrackListFragmentViewModel.class)
    abstract ViewModel bindTrackListFragmentViewModel(TrackListFragmentViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(MapFragmentViewModel.class)
    abstract ViewModel bindMapFragmentViewModel(MapFragmentViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(CompassFragmentViewModel.class)
    abstract ViewModel bindCompassFragmentViewModel(CompassFragmentViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(GpsStatusActivityViewModel.class)
    abstract ViewModel bindGpsStatusActivityViewModel(GpsStatusActivityViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(GpsStatusFragmentViewModel.class)
    abstract ViewModel bindGpsStatusFragmentViewModel(GpsStatusFragmentViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(MapDownloadActivityViewModel.class)
    abstract ViewModel bindMapDownloadActivityViewModel(MapDownloadActivityViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(MapDownloadFragmentViewModel.class)
    abstract ViewModel bindMapDownloadFragmentViewModel(MapDownloadFragmentViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(ImportSettingsDialogViewModel.class)
    abstract ViewModel bindImportSettingsDialogViewModel(ImportSettingsDialogViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(SelectTracksToImportDialogViewModel.class)
    abstract ViewModel bindSelectTracksToImportDialogViewModel(SelectTracksToImportDialogViewModel viewModel);
}
