package nl.erikduisters.pathfinder.di;

import javax.inject.Singleton;

import dagger.Component;
import dagger.android.AndroidInjector;
import dagger.android.support.AndroidSupportInjectionModule;
import nl.erikduisters.pathfinder.MyApplication;

/**
 * Created by Erik Duisters on 01-06-2018.
 */

@Component(modules = {AndroidSupportInjectionModule.class, AppModule.class, ActivityBindingModule.class, ViewModelBindingModule.class})
@Singleton
public interface AppComponent extends AndroidInjector<MyApplication> {
    @Component.Builder
    abstract class Builder extends AndroidInjector.Builder<MyApplication> {
    }
}
