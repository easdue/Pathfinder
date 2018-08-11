package nl.erikduisters.pathfinder.ui.activity.settings;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import nl.erikduisters.pathfinder.di.FragmentScope;
import nl.erikduisters.pathfinder.ui.fragment.settings.SettingsFragment;

/**
 * Created by Erik Duisters on 20-07-2018.
 */
@Module
public abstract class SettingsActivityBindingModule {
    @FragmentScope
    @ContributesAndroidInjector
    abstract SettingsFragment contributeSettingsFragmentInjector();

}
