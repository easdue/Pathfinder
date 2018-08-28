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
import nl.erikduisters.pathfinder.service.MapDownloadService;
import nl.erikduisters.pathfinder.service.gpsies_service.GPSiesService;
import nl.erikduisters.pathfinder.service.track_import.TrackImportService;
import nl.erikduisters.pathfinder.ui.app_widget.WidgetService;

/**
 * Created by Erik Duisters on 28-07-2018.
 */

@Module
public abstract class ServiceModule {
    @ContributesAndroidInjector
    abstract MapDownloadService bindDownloadService();

    @ContributesAndroidInjector
    abstract GPSiesService bindGPSiesService();

    @ContributesAndroidInjector
    abstract TrackImportService bindTrackImportService();

    @ContributesAndroidInjector
    abstract WidgetService bindWidgetService();
}
