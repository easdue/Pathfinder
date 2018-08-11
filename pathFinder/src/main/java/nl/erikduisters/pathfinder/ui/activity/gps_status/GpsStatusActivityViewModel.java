package nl.erikduisters.pathfinder.ui.activity.gps_status;

import android.Manifest;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import javax.inject.Inject;
import javax.inject.Singleton;

import nl.erikduisters.pathfinder.data.local.PreferenceManager;
import nl.erikduisters.pathfinder.ui.BaseActivityViewModel;
import nl.erikduisters.pathfinder.ui.fragment.runtime_permission.RuntimePermissionHelper;

/**
 * Created by Erik Duisters on 17-07-2018.
 */

@Singleton
public class GpsStatusActivityViewModel extends BaseActivityViewModel {
    private final MutableLiveData<GpsStatusActivityViewState> viewStateObservable;

    @Inject
    GpsStatusActivityViewModel(PreferenceManager preferenceManager) {
        super(preferenceManager);
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
