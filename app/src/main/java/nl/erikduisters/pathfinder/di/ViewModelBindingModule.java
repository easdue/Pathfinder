package nl.erikduisters.pathfinder.di;

import android.arch.lifecycle.ViewModel;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;
import nl.erikduisters.pathfinder.ui.activity.main_activity.MainActivityViewModel;
import nl.erikduisters.pathfinder.ui.fragment.init_storage.InitStorageFragmentViewModel;
import nl.erikduisters.pathfinder.ui.fragment.play_services.PlayServicesFragmentViewModel;
import nl.erikduisters.pathfinder.ui.fragment.runtime_permission.RuntimePermissionFragmentViewModel;

/**
 * Created by Erik Duisters on 01-06-2018.
 */

@Module
abstract class ViewModelBindingModule {
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
}
