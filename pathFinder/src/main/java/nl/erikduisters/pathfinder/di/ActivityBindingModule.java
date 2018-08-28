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

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import nl.erikduisters.pathfinder.ui.activity.gps_status.GpsStatusActivity;
import nl.erikduisters.pathfinder.ui.activity.gps_status.GpsStatusActivityBindingModule;
import nl.erikduisters.pathfinder.ui.activity.main_activity.MainActivity;
import nl.erikduisters.pathfinder.ui.activity.main_activity.MainActivityBindingModule;
import nl.erikduisters.pathfinder.ui.activity.map_download.MapDownloadActivity;
import nl.erikduisters.pathfinder.ui.activity.map_download.MapDownloadActivityBindingModule;
import nl.erikduisters.pathfinder.ui.activity.settings.SettingsActivity;
import nl.erikduisters.pathfinder.ui.activity.settings.SettingsActivityBindingModule;

/**
 * Created by Erik Duisters on 01-06-2018.
 */

@Module
abstract class ActivityBindingModule {
    @ContributesAndroidInjector(modules = MainActivityBindingModule.class)
    abstract MainActivity contributeMainActivity();

    @ContributesAndroidInjector(modules = GpsStatusActivityBindingModule.class)
    abstract GpsStatusActivity contributeGpsStatusActivity();

    @ContributesAndroidInjector(modules = SettingsActivityBindingModule.class)
    abstract SettingsActivity contributeSettingsActivity();

    @ContributesAndroidInjector(modules = MapDownloadActivityBindingModule.class)
    abstract MapDownloadActivity contributeMapDownloadActivity();
}
