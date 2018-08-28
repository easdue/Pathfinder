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

import nl.erikduisters.pathfinder.ui.activity.FragmentProvider;
import nl.erikduisters.pathfinder.ui.fragment.ViewPagerFragment;
import nl.erikduisters.pathfinder.ui.fragment.compass.CompassFragment;
import nl.erikduisters.pathfinder.ui.fragment.map.MapFragment;
import nl.erikduisters.pathfinder.ui.fragment.track_list.TrackListFragment;

/**
 * Created by Erik Duisters on 27-06-2018.
 */
enum MainActivityFragmentProvider implements FragmentProvider {
    TRACK_LIST_FRAGMENT {
        @Override
        public ViewPagerFragment provideFragment() {
            return TrackListFragment.newInstance();
        }
    },
    MAP_FRAGMENT {
        @Override
        public ViewPagerFragment provideFragment() {
            return MapFragment.newInstance();
        }
    },
    COMPASS_FRAGMENT {
        @Override
        public ViewPagerFragment provideFragment() {
            return CompassFragment.newInstance();
        }
    }
}
