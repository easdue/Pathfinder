package nl.erikduisters.pathfinder.ui.activity.gps_status;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import nl.erikduisters.pathfinder.di.FragmentScope;
import nl.erikduisters.pathfinder.ui.fragment.gps_status.GpsStatusFragment;

/**
 * Created by Erik Duisters on 17-07-2018.
 */

@Module
public abstract class GpsStatusActivityBindingModule {
    @FragmentScope
    @ContributesAndroidInjector
    abstract GpsStatusFragment contributeGpsStatusFragmentInjector();
}
