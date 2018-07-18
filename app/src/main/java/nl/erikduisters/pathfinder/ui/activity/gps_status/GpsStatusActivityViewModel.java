package nl.erikduisters.pathfinder.ui.activity.gps_status;

import android.Manifest;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import javax.inject.Inject;
import javax.inject.Singleton;

import nl.erikduisters.pathfinder.ui.fragment.runtime_permission.RuntimePermissionHelper;

/**
 * Created by Erik Duisters on 17-07-2018.
 */

@Singleton
public class GpsStatusActivityViewModel extends ViewModel {
    private final MutableLiveData<GpsStatusActivityViewState> viewStateObservable;

    @Inject
    GpsStatusActivityViewModel() {
        viewStateObservable = new MutableLiveData<>();
    }

    void setRuntimePermissionHelper(RuntimePermissionHelper helper) {
        if (helper.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            viewStateObservable.setValue(new GpsStatusActivityViewState.InitializedState());
        } else {
            viewStateObservable.setValue(new GpsStatusActivityViewState.FinishState());
        }
    }

    LiveData<GpsStatusActivityViewState> getViewStateObservable() { return viewStateObservable; }
}
