package nl.erikduisters.pathfinder.ui.activity.main_activity;

import nl.erikduisters.pathfinder.ui.activity.FragmentProvider;
import nl.erikduisters.pathfinder.ui.activity.ViewPagerFragment;
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
