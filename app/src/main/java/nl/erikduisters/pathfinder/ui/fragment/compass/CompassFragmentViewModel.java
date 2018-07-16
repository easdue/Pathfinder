package nl.erikduisters.pathfinder.ui.fragment.compass;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.location.Location;
import android.support.annotation.NonNull;

import javax.inject.Inject;
import javax.inject.Singleton;

import nl.erikduisters.pathfinder.data.local.GpsManager;
import nl.erikduisters.pathfinder.data.local.HeadingManager;
import nl.erikduisters.pathfinder.data.local.PreferenceManager;
import nl.erikduisters.pathfinder.util.Distance;
import nl.erikduisters.pathfinder.util.IntegerDegrees;
import nl.erikduisters.pathfinder.util.Speed;
import nl.erikduisters.pathfinder.util.UnitsUtil;
import nl.erikduisters.pathfinder.util.menu.MyMenu;
import timber.log.Timber;

/**
 * Created by Erik Duisters on 28-06-2018.
 */

//TODO: TimeToNext, ETA
//TODO: Keep screen on
@Singleton
public class CompassFragmentViewModel extends ViewModel implements GpsManager.LocationListener, HeadingManager.HeadingListener {
    private final MutableLiveData<CompassFragmentViewState> viewStateObservable;

    private final PreferenceManager preferenceManager;
    private final GpsManager gpsManager;
    private final HeadingManager headingManager;

    private CompassFragmentViewState.Builder builder;

    @Inject
    CompassFragmentViewModel(PreferenceManager preferenceManager, GpsManager gpsManager, HeadingManager headingManager) {
        this.preferenceManager = preferenceManager;
        this.gpsManager = gpsManager;
        this.headingManager = headingManager;

        viewStateObservable = new MutableLiveData<>();

        initBuilder();

        viewStateObservable.setValue(builder.build());
    }

    public LiveData<CompassFragmentViewState> getViewStateObservable() { return viewStateObservable; }

    private void initBuilder() {
        builder = new CompassFragmentViewState.Builder()
                .withOptionsMenu(createOptionsMenu())
                .withBearing(new IntegerDegrees())
                .withHeading(new IntegerDegrees())
                .withDistanceToNext(new Distance(Distance.UNKNOWN_DISTANCE, 0, preferenceManager.getUnits()))
                .withSpeed(new Speed(0, 0, preferenceManager.getUnits()));
    }

    private MyMenu createOptionsMenu() {
        //TODO: Add menu items
        return new MyMenu();
    }

    void start() {
        Timber.d("Start listening to sensors");
        gpsManager.addLocationListener(this);

        headingManager.addHeadingListener(this);
    }

    void stop() {
        Timber.d("stop listening to sensors");
        gpsManager.removeLocationListener(this);

        headingManager.removeHeadingListener(this);
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        double speed = UnitsUtil.metersPerSecond2KilometersPerHour(location.hasSpeed() ? location.getSpeed() : 0);

        builder.withSpeed(new Speed(speed, 2, preferenceManager.getUnits()));

        viewStateObservable.setValue(builder.build());
    }

    @Override
    public void onHeadingChanged(IntegerDegrees heading) {
        Timber.e("onHeadingChanged");
        builder.withHeading(heading);

        viewStateObservable.setValue(builder.build());
    }
}
