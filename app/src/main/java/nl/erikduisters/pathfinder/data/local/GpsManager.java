package nl.erikduisters.pathfinder.data.local;

import android.annotation.TargetApi;
import android.arch.lifecycle.DefaultLifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.ProcessLifecycleOwner;
import android.location.GnssStatus;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import timber.log.Timber;

//TODO: User localbroadcast instead of listeners
//TODO: Update to the new GNSS but there are issues see comment in SatelliteInfo below
/**
 * Created by Erik Duisters on 03-01-2017.
 */
@Singleton
public class GpsManager implements
        DefaultLifecycleObserver {

    private static final String TAG = GpsManager.class.getSimpleName();

    private interface GpsStatusListener {
        void setGpsStatusChangedListener(GpsStatusChangedListener listener);

        boolean isGpsStatusChangedListenerSet();
    }

    public interface GpsStatusChangedListener {
        void onSatelliteStatusChanged(@NonNull ArrayList<SatelliteInfo> satInfoList);
        void onGpsStarted();
        void onGpsStopped();
    }

    private static final long NORMAL_UPDATE_MILLISECONDS = 1000;
    private static final long NORMAL_UPDATE_FIX_TIMEOUT = 10000;
    private static final long SLOW_UPDATE_MILLISECONDS = 30000;
    private static final long SLOW_UPDATE_FIX_TIMEOUT = 90000;
    private static final long FAST_UPDATE_MILLISECONDS = 800;

    @IntDef({StateType.TYPE_FAST, StateType.TYPE_SLOW, StateType.TYPE_STOPPED})
    @Retention(RetentionPolicy.SOURCE)
    private @interface StateType {
        int TYPE_STOPPED = 0;
        int TYPE_SLOW = 1;
        int TYPE_FAST = 2;
    }

    private PreferenceManager preferenceManager;
    private LocationManager locationManager;
    private LocationRequest locationRequest;
    private Location lastLocation;
    private boolean gpsHasFix;
    private final List<LocationListener> locationListeners;
    private final List<GpsFixListener> gpsFixListeners;
    private GpsStatusListener gpsStatusListener;
    private @StateType int state = StateType.TYPE_STOPPED;
    private boolean accessFineLocationPermitted;
    private boolean googlePlayServicesAvailable;
    private final Handler handler;
    private final Runnable checkFixRunnable;
    private boolean checkFixRunnablePosted = false;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private CombinedLocationListener combinedLocationListener;
    private boolean waitingForFusedLocationProvider;

    @Inject
    GpsManager(PreferenceManager preferenceManager, LocationManager locationManager, FusedLocationProviderClient fusedLocationProviderClient) {
        //TODO: Optionally also use NETWORK_PROVIDER when not using location services and GPS_PROVIDER has no fix
        Timber.d("New GpsManager created");

        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);

        this.preferenceManager = preferenceManager;
        this.locationManager = locationManager;
        this.fusedLocationProviderClient = fusedLocationProviderClient;
        this.checkFixRunnable = this::checkFix;

        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(NORMAL_UPDATE_MILLISECONDS);
        locationRequest.setFastestInterval(FAST_UPDATE_MILLISECONDS);

        combinedLocationListener = new CombinedLocationListener();

        locationListeners = new ArrayList<>();
        gpsFixListeners = new ArrayList<>();

        /* TODO: Until I have a phone with API >= 24 I can't test the GNSS implementation because emulators don't provide useful information
        if (Build.VERSION.SDK_INT < 24) {
            gpsStatusListener = new PreApi24StatusListener(locationManager);
        } else {
            gpsStatusListener = new PostApi24StatusListener();
        }
        */
        gpsStatusListener = new PreApi24StatusListener(locationManager);

        handler = new Handler();
    }

    public LocationRequest getLocationRequest() {
        return locationRequest;
    }

    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
        requestFastLocationUpdates();
    }

    @Override
    public void onStop(@NonNull LifecycleOwner owner) {
        requestSlowLocationUpdates();
    }

    /**
     * Pauses fast location update reception.
     * @throws SecurityException if permission ACCESS_FINE_LOCATION has not been granted
     */
    public void requestSlowLocationUpdates() throws SecurityException {
        Timber.d("requestSlowLocationUpdates");
        if (lastLocation != null) {
            preferenceManager.setLastKnownLocation(lastLocation);
        }

        if (state != StateType.TYPE_SLOW) {
            state = StateType.TYPE_SLOW;

            initLocationUpdates();
        }
    }

    /**
     * Resume fast location update reception
     * @throws SecurityException if permission ACCESS_FINE_LOCATION has not been granted
     */
    public void requestFastLocationUpdates() throws SecurityException {
        Timber.d("requestFastLocationUpdates()");

        if (state != StateType.TYPE_FAST) {
            state = StateType.TYPE_FAST;

            initLocationUpdates();
        }
    }

    public void onAccessFineLocationPermitted() {
        accessFineLocationPermitted = true;

        initLocationUpdates();
        initGpsStatusUpdates();
    }

    public void onGooglePlayServicesAvailable() {
        googlePlayServicesAvailable = true;

        initLocationUpdates();
    }

    private void initLocationUpdates() throws SecurityException {
        Timber.d("initLocationUpdates()");

        if (state == StateType.TYPE_STOPPED || !accessFineLocationPermitted) {
            Timber.d("    STOPPED or AccessFineLocation Not Permitted");
            return;
        }

        if (lastLocation == null) {
            initLastLocation();
        }

        long interval = state == StateType.TYPE_FAST ? NORMAL_UPDATE_MILLISECONDS : SLOW_UPDATE_MILLISECONDS;

        if (googlePlayServicesAvailable) {
            if (!waitingForFusedLocationProvider) {
                waitingForFusedLocationProvider = true;
                locationRequest.setInterval(interval);

                fusedLocationProviderClient
                        .requestLocationUpdates(locationRequest, combinedLocationListener, null)
                        .addOnSuccessListener(aVoid -> {
                            Timber.d("Using Google Play Services fusedLocationProvider");

                            waitingForFusedLocationProvider = false;
                            locationManager.removeUpdates(combinedLocationListener);
                        })
                        .addOnCanceledListener(() -> {
                            Timber.d("RequestLocationUpdates was Canceled");

                            googlePlayServicesAvailable = false;
                            waitingForFusedLocationProvider = false;
                            initLocationUpdates();
                        })
                        .addOnFailureListener(e -> {
                            Timber.d("RequestLocationUpdates failed");

                            googlePlayServicesAvailable = false;
                            waitingForFusedLocationProvider = false;
                            initLocationUpdates();
                        });
            }
        } else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Timber.d("Using LocationManager");

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, interval, 0, combinedLocationListener);
        }

        if (gpsHasFix) {
            scheduleFixCheck(true);
        }
    }

    private void initLastLocation() throws SecurityException {
        Timber.d("initLastLocation()");

        Location location = null;

        if (!accessFineLocationPermitted) {
            return;
        }

        if (!googlePlayServicesAvailable) {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                handleLastLocation(location);
            }
        } else {
            fusedLocationProviderClient
                    .getLastLocation()
                    .addOnSuccessListener(this::handleLastLocation);
        }
    }

    private void handleLastLocation(Location location) {
        Location locationFromPrefs = preferenceManager.getLastKnownLocation();

        if (location == null) {
            if (locationFromPrefs.getLatitude() != -91 && locationFromPrefs.getLongitude() != -181) {
                lastLocation = locationFromPrefs;
            } else {
                //TODO: use user profile location or address if available
                lastLocation = new Location(LocationManager.GPS_PROVIDER);
                lastLocation.setTime(System.currentTimeMillis());

                lastLocation.setLatitude(52.3700); //Amsterdam
                lastLocation.setLongitude(4.8900);
            }
        } else if (locationFromPrefs.getLatitude() == -91 || locationFromPrefs.getLongitude() == -181) {
            lastLocation = location;
        } else {
            lastLocation = (location.getTime() > locationFromPrefs.getTime()) ? location : locationFromPrefs;
        }

        lastLocation.removeSpeed();

        notifyLocationListeners();
        notifyGpsFixListeners();    //Last location doesn't mean there is a fix
    }

    /**
     * Stops reception of location updates
     * @throws SecurityException if permission ACCESS_FINE_LOCATION has not been granted
     */
    public void stopLocationUpdates() throws SecurityException {
        Timber.d("stopLocationUpdates()");

        state = StateType.TYPE_STOPPED;

        if (googlePlayServicesAvailable) {
            fusedLocationProviderClient.removeLocationUpdates(combinedLocationListener);
        } else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.removeUpdates(combinedLocationListener);
        }

        handler.removeCallbacks(checkFixRunnable);
        checkFixRunnablePosted = false;
        gpsHasFix = false;

        notifyGpsFixListeners();
    }

    private void scheduleFixCheck(boolean force) {
        if (!force && checkFixRunnablePosted) {
            return;
        }

        if (checkFixRunnablePosted) {
            handler.removeCallbacks(checkFixRunnable);
            checkFixRunnablePosted = false;
        }

        long delay;

        switch (this.state) {
            case StateType.TYPE_FAST:
                delay = NORMAL_UPDATE_FIX_TIMEOUT;
                break;
            case StateType.TYPE_SLOW:
                delay = SLOW_UPDATE_FIX_TIMEOUT;
                break;
            default:
                delay = 0;
                break;
        }

        if (delay != 0) {
            Timber.d("Scheduling checkFix, delay=%d", delay);
            handler.postDelayed(checkFixRunnable, delay);
            checkFixRunnablePosted = true;
        }
    }

    private void checkFix() {
        Timber.d("checkFix()");
        checkFixRunnablePosted = false;

        if (!gpsHasFix) {
            return;
        }

        if (lastLocation != null) {
            long timeout = (state == StateType.TYPE_FAST) ? NORMAL_UPDATE_FIX_TIMEOUT : SLOW_UPDATE_FIX_TIMEOUT;

            long timeDiff;

            if (Build.VERSION.SDK_INT < 17) {
                timeDiff = System.currentTimeMillis() - lastLocation.getTime();
            } else {
                timeDiff = (android.os.SystemClock.elapsedRealtimeNanos() - lastLocation.getElapsedRealtimeNanos()) / 1000000L;
            }

            if (timeDiff > timeout) {
                Timber.d("checkFix: GPS fix lost, diff: %d", timeDiff);
                gpsHasFix = false;
                notifyGpsFixListeners();

                return;
            }
        }

        /* If we've reached this point, we still have a fix so schedule another check */
        scheduleFixCheck(false);
    }

    /**
     * Checks if the gps has been enabled on the device
     * @return true if gps is enabled false if not
     * @throws SecurityException if permission ACCESS_FINE_LOCATION has not been granted
     */
    public boolean isGpsEnabled() throws SecurityException {
        return accessFineLocationPermitted && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public boolean hasGps() throws SecurityException {
        return accessFineLocationPermitted && locationManager.getProvider(LocationManager.GPS_PROVIDER) != null;
    }

    public boolean gpsHasFix() {
        return gpsHasFix;
    }

    /**
     * Gets the lost know location
     * @return The last know location or null
     */
    public @Nullable Location getLastKnowLocation() {
        return lastLocation;
    }

    public interface LocationListener {
        void onLocationChanged(@NonNull Location location);
    }

    public interface GpsFixListener {
        void onGpsFixAcquired();
        void onGpsFixLost();
    }

    public void addLocationListener(@NonNull LocationListener listener) {
        if (!locationListeners.contains(listener)) {
            locationListeners.add(listener);

            if (lastLocation != null) {
                listener.onLocationChanged(lastLocation);
            }
        }
    }

    public void removeLocationListener(@NonNull LocationListener listener) {
        locationListeners.remove(listener);
    }

    public void addGpsFixListener(@NonNull GpsFixListener listener) {
        if (!gpsFixListeners.contains(listener)) {
            gpsFixListeners.add(listener);

            if (gpsHasFix) {
                listener.onGpsFixAcquired();
            } else {
                listener.onGpsFixLost();
            }
        }
    }

    public void removeGpsFixListener(@NonNull GpsFixListener listener) {
        gpsFixListeners.remove(listener);
    }

    private void notifyLocationListeners() {
        for (LocationListener listener : locationListeners) {
            listener.onLocationChanged(lastLocation);
        }
    }

    private void notifyGpsFixListeners() {
        for (GpsFixListener listener : gpsFixListeners) {
            notifyGpsFixListener(listener);
        }

    }

    private void notifyGpsFixListener(GpsFixListener listener) {
        if (gpsHasFix) {
            listener.onGpsFixAcquired();
        } else {
            listener.onGpsFixLost();
        }
    }

    private void initGpsStatusUpdates() throws SecurityException {
        if (!accessFineLocationPermitted) {
            return;
        }

        if (gpsStatusListener.isGpsStatusChangedListenerSet()) {
            /*
            if (Build.VERSION.SDK_INT < 24) {
                //noinspection deprecation
                locationManager.addGpsStatusListener((PreApi24StatusListener)gpsStatusListener);
            } else {
                locationManager.registerGnssStatusCallback((PostApi24StatusListener)gpsStatusListener);
            }
            */

            locationManager.addGpsStatusListener((PreApi24StatusListener) gpsStatusListener);
        }
    }

    public void setGpsStatusChangedListener(GpsStatusChangedListener listener) throws SecurityException {
        gpsStatusListener.setGpsStatusChangedListener(listener);

        if (listener != null) {
            initGpsStatusUpdates();
        } else if (accessFineLocationPermitted){
            /*
            if (Build.VERSION.SDK_INT < 24) {
                //noinspection deprecation
                locationManager.removeGpsStatusListener((PreApi24StatusListener)gpsStatusListener);
            } else {
                locationManager.unregisterGnssStatusCallback((PostApi24StatusListener)gpsStatusListener);
            }
            */
            //noinspection deprecation
            locationManager.removeGpsStatusListener((PreApi24StatusListener) gpsStatusListener);
        }
    }

    private class CombinedLocationListener
            extends LocationCallback
            implements android.location.LocationListener {

        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);

            Timber.d("onLocationResult - NumLocations: %d", locationResult.getLocations().size());
            onLocationChanged(locationResult.getLastLocation());

        }

        @Override
        public void onLocationChanged(Location location) {
            Timber.d("onLocationChanged received: %s", location == null ? "null" : location.toString());

            if (location == null) {
                return;
            }

            lastLocation = location;

            if (!gpsHasFix) {
                gpsHasFix = true;
                notifyGpsFixListeners();
            }

            notifyLocationListeners();

            if (state == StateType.TYPE_SLOW) {
                preferenceManager.setLastKnownLocation(location);
            }

            scheduleFixCheck(false);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            //Don't care
        }

        @Override
        public void onProviderEnabled(String provider) {
            //Don't care
        }

        @Override
        public void onProviderDisabled(String provider) {
            //Don't care
        }
    }

    @SuppressWarnings("deprecation")
    private static class PreApi24StatusListener implements GpsStatusListener, GpsStatus.Listener {
        private LocationManager locationManager;
        private GpsStatusChangedListener listener;
        private GpsStatus lastGpsStatus;

        PreApi24StatusListener(LocationManager manager) { locationManager = manager; }

        @Override
        public void setGpsStatusChangedListener(GpsStatusChangedListener listener) {
            this.listener = listener;
        }

        @Override
        public boolean isGpsStatusChangedListenerSet() {
            return listener != null;
        }

        @Override
        public void onGpsStatusChanged(int event) {
            switch (event) {
                case GpsStatus.GPS_EVENT_STARTED:
                    if (listener != null) {
                        listener.onGpsStarted();
                    }
                    break;
                case GpsStatus.GPS_EVENT_STOPPED:
                    if (listener != null) {
                        listener.onGpsStopped();
                    }
                    break;
                case GpsStatus.GPS_EVENT_FIRST_FIX:
                    break;
                case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                    try {
                        if (lastGpsStatus == null) {
                            lastGpsStatus = locationManager.getGpsStatus(null);
                        } else {
                            locationManager.getGpsStatus(lastGpsStatus);
                        }

                        if (listener != null && lastGpsStatus != null) {
                            ArrayList<SatelliteInfo> satInfoList;

                            Iterable<GpsSatellite> satellites = lastGpsStatus.getSatellites();

                            satInfoList = new ArrayList<>();

                            for (GpsSatellite sat : satellites) {
                                satInfoList.add(new SatelliteInfo(sat));
                            }
                            Collections.sort(satInfoList, new CompareGpsSatellites());

                            listener.onSatelliteStatusChanged(satInfoList);
                                                    }
                    } catch (SecurityException e) {
                        //Handled in setGpsStatusChangedListener
                    }
                    break;
            }
        }
    }

    /*
    @RequiresApi(24)
    private static class PostApi24StatusListener extends GnssStatus.Callback implements GpsStatusListener {
        private GpsStatusChangedListener listener;

        PostApi24StatusListener() {
            super();
        }

        @Override
        public boolean isGpsStatusChangedListenerSet() {
            return listener != null;
        }

        @Override
        public void onSatelliteStatusChanged(GnssStatus status) {
            int size = status.getSatelliteCount();

            ArrayList<SatelliteInfo> satInfoList = new ArrayList<>(size);

            for (int i = 0; i < size; i++) {
                satInfoList.add(new SatelliteInfo(status, i));
            }

            Collections.sort(satInfoList, new CompareGpsSatellites());

            if (listener != null) {
                listener.onSatelliteStatusChanged(satInfoList);
            }
        }

        @Override
        public void onStarted() {
            if (listener != null) {
                listener.onGpsStarted();
            }
        }

        @Override
        public void onStopped() {
            if (listener != null) {
                listener.onGpsStopped();
            }
        }

        @Override
        public void setGpsStatusChangedListener(GpsStatusChangedListener listener) {
            this.listener = listener;
        }
    }
    */

    private static class CompareGpsSatellites implements Comparator<SatelliteInfo> {
        @Override
        public int compare(SatelliteInfo sat1, SatelliteInfo sat2) {
            if (sat1.snr == sat2.snr) {
                if (sat1.usedInFix && !sat2.usedInFix) {
                    return -1;
                } else if (!sat1.usedInFix && sat2.usedInFix) {
                    return 1;
                }
                return 0;
            } else if (sat1.snr > sat2.snr) {
                return -1;
            } else {
                return 1;
            }
        }
    }

    public static class SatelliteInfo {
        public static final int CONSTELLATION_GPS = 1;
        public static final int CONSTELLATION_SBAS = 2;
        public static final int CONSTELLATION_GLONASS = 3;
        public static final int CONSTELLATION_QZSS = 4;
        public static final int CONSTELLATION_BEIDOU = 5;
        public static final int CONSTELLATION_GALILEO = 6;
        public static final int CONSTELLATION_UNKNOWN = 7;

        @IntDef({CONSTELLATION_GPS, CONSTELLATION_SBAS, CONSTELLATION_GLONASS, CONSTELLATION_QZSS, CONSTELLATION_BEIDOU, CONSTELLATION_GALILEO, CONSTELLATION_UNKNOWN})
        @Retention(RetentionPolicy.SOURCE)
        public @interface Constellation {
        }

        public @Constellation int constellation;
        public float azimuth;
        public float elevation;
        public int prn;
        public float snr;
        public boolean hasAlmanac;
        public boolean hasEphemeris;
        public boolean usedInFix;

        public SatelliteInfo(@SuppressWarnings("deprecation") GpsSatellite sat) {
            constellation = CONSTELLATION_UNKNOWN;
            azimuth = sat.getAzimuth();
            elevation = sat.getElevation();
            prn = sat.getPrn();
            snr = sat.getSnr();
            hasAlmanac = sat.hasAlmanac();
            hasEphemeris = sat.hasEphemeris();
            usedInFix = sat.usedInFix();
        }

        /*
            https://developer.android.com/reference/android/location/GnssMeasurement.html#getCn0DbHz()
            Cn0DbHz is in the range of 10-50 DbHz

            http://gauss.gge.unb.ca/papers.pdf/SNR.memo.pdf

            SNC SNR(db:1khz) C/N0(db:1Hz)
              3    6.5         36.5 Very weak signal
              5   11           41
             10   17           47
             20   23           53
             30   26.5         56.5
             40   29           59 Very strong signal

             https://github.com/barbeau/gpstest/blob/master/GPSTest/src/main/java/com/android/gpstest/view/GpsSkyView.java
             mSnrThresholds = new float[]{0.0f, 10.0f, 20.0f, 30.0f};
             mCn0Thresholds = new float[]{10.0f, 21.67f, 33.3f, 45.0f};
        */
        @TargetApi(Build.VERSION_CODES.N)
        public SatelliteInfo(GnssStatus gnssStatus, int satIndex) {
            switch (gnssStatus.getConstellationType(satIndex)) {
                case GnssStatus.CONSTELLATION_BEIDOU:
                    constellation = CONSTELLATION_BEIDOU;
                    break;
                case GnssStatus.CONSTELLATION_GALILEO:
                    constellation = CONSTELLATION_GALILEO;
                    break;
                case GnssStatus.CONSTELLATION_GLONASS:
                    constellation = CONSTELLATION_GLONASS;
                    break;
                case GnssStatus.CONSTELLATION_GPS:
                    constellation = CONSTELLATION_GPS;
                    break;
                case GnssStatus.CONSTELLATION_QZSS:
                    constellation = CONSTELLATION_QZSS;
                    break;
                case GnssStatus.CONSTELLATION_SBAS:
                    constellation = CONSTELLATION_SBAS;
                    break;
                default:
                    constellation = CONSTELLATION_UNKNOWN;
                    break;
            }

            azimuth = gnssStatus.getAzimuthDegrees(satIndex);
            elevation = gnssStatus.getElevationDegrees(satIndex);
            prn = gnssStatus.getSvid(satIndex);
            //TODO: I need registerGnssMeasurementsCallback to get the snr but on at least Nexus 5x null is returned
            //      GPS Test uses Cn0DbHz because of that
            snr = gnssStatus.getCn0DbHz(satIndex);
            hasAlmanac = gnssStatus.hasAlmanacData(satIndex);
            hasEphemeris = gnssStatus.hasEphemerisData(satIndex);
            usedInFix = gnssStatus.usedInFix(satIndex);
        }
    }
}