package nl.erikduisters.pathfinder.di;

import android.arch.lifecycle.ViewModel;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;
import nl.erikduisters.pathfinder.ui.activity.main_activity.MainActivityViewModel;
import nl.erikduisters.pathfinder.ui.fragment.init_storage.InitStorageFragmentViewModel;

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
}
