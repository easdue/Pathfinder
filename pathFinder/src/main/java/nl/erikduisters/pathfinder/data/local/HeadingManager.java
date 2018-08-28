/*
 * Copyright (c) 2018 Erik Duisters
 *
 * This file is part of Pathfinder
 *
 * Pathfinder is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Foobar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Pathfinder. If not, see <https://www.gnu.org/licenses/>.
 */

package nl.erikduisters.pathfinder.data.local;

import android.content.Context;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.view.Surface;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;
import javax.inject.Singleton;

import nl.erikduisters.pathfinder.di.ApplicationContext;
import nl.erikduisters.pathfinder.util.IntegerDegrees;
import nl.erikduisters.pathfinder.util.UnitsUtil;
import timber.log.Timber;

/*
 * Created by Erik Duisters on 13-07-2018.
 */

@Singleton
public class HeadingManager implements SensorEventListener, GpsManager.LocationListener, GpsManager.GpsFixListener {
    private static final float CALC_DECLINATION_EVERY_X_METERS = 500;

    public interface HeadingListener {
        /**
         * onHeadingChanged is called whenever a heading change has been detected
         *
         * @param heading The heading in degrees (0-360) or -1 if unknown
         */
        void onHeadingChanged(IntegerDegrees heading);
    }

    private final float[] rotationMatrix = new float[16];
    private final float[] remappedMatrix = new float[16];
    private final float[] orientationValues = new float[3];
    private final float[] truncatedRotationVector = new float[4];
    private float[] magneticValues = new float[]{0f, 0f, 0f};
    private float[] acceleroValues = new float[]{0f, 0f, 0f};
    private IntegerDegrees heading = new IntegerDegrees();
    private float newHeadingFloat;
    private int newHeadingInt;
    private boolean magneticValuesInitialized = false;
    private boolean acceleroValuesInitialized = false;
    private boolean truncateVector = false;
    private float declination = Float.NaN;
    private Location declinationCalculatedAt;
    private SensorManager sensorManager = null;
    private final ArrayList<Sensor> sensorList = new ArrayList<>();
    private boolean useTrueNorth;
    private boolean useGpsForHeading;
    private int gpsHeadingMinSpeedKmh;
    private int gpsHeadingMinSpeedSeconds;
    private double gpsHeadingSpeedMatchSeconds = -1;
    private Location prevLocation = null;
    private final AtomicBoolean computing = new AtomicBoolean(false);
    private boolean listeningToSensors = false;
    private final ArrayList<HeadingListener> listeners = new ArrayList<>();
    private final PreferenceManager preferenceManager;
    private final GpsManager gpsManager;
    private final WindowManager windowManager;

    @Inject
    public HeadingManager(@ApplicationContext Context context, PreferenceManager preferenceManager, GpsManager gpsManager) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        this.preferenceManager = preferenceManager;
        this.gpsManager = gpsManager;

        Sensor rotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        if (rotationVector != null) {
            sensorList.add(rotationVector);
        } else {
            Sensor acceleroMeter = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            Sensor magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            if (acceleroMeter != null && magneticField != null) {
                sensorList.add(acceleroMeter);
                sensorList.add(magneticField);
            }
        }
    }

    private void startListeningToSensors() {
        if (sensorList.size() > 0) {
            for (Sensor s : sensorList) {
                sensorManager.registerListener(this, s, SensorManager.SENSOR_DELAY_UI);
                Timber.v("Started using sensor: %s", s.getName());
            }

            listeningToSensors = true;
        }
    }

    private void stopListeningToSensors() {
        if (sensorList.size() > 0) {
            for (Sensor s : sensorList) {
                sensorManager.unregisterListener(this, s);
                Timber.v("Stopped using sensor: %s", s.getName());
            }
        }
        listeningToSensors = false;
    }

    private void onResume() {
        Timber.d("onResume()");

        startListeningToSensors();

        useTrueNorth = preferenceManager.getUseTrueNorth();
        useGpsForHeading = preferenceManager.getUseGpsBearing();
        gpsHeadingMinSpeedKmh = preferenceManager.getUseGpsBearingSpeed();
        gpsHeadingMinSpeedSeconds = preferenceManager.getUsGpsBearingDuration();

        if (!listeningToSensors) {
            useGpsForHeading = true;
            gpsHeadingMinSpeedKmh = 0;
            gpsHeadingMinSpeedSeconds = 0;
        }

        if (useTrueNorth || useGpsForHeading) {
            gpsManager.addLocationListener(this);
            gpsManager.addGpsFixListener(this);
        }

        updateListeners();
    }

    private void onPause() {
        Timber.d("onPause()");

        stopListeningToSensors();

        gpsManager.removeLocationListener(this);
        gpsManager.removeGpsFixListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        boolean gotRotationMatrix = false;

        if (!computing.compareAndSet(false, true)) {
            return;
        }

        switch (event.sensor.getType()) {
            case Sensor.TYPE_ROTATION_VECTOR:
                if (!truncateVector) {
                    try {
                        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
                        gotRotationMatrix = true;
                    } catch (IllegalArgumentException e) {
                        /* On some Samsung devices, an exception is thrown if event.values contains
                         * more than 4 values.
                         */
                        //LogUtil.e(TAG, "Samsung device error? Will truncate vectors: " + e.getMessage());
                        truncateVector = true;
                        getRotationMatrixFromTruncatedVector(event.values);
                        gotRotationMatrix = true;
                    }
                } else {
                    getRotationMatrixFromTruncatedVector(event.values);
                    gotRotationMatrix = true;
                }

                break;
            case Sensor.TYPE_ACCELEROMETER:
                /* filtering also isolates gravity */
                acceleroValues = LowPassFilter.filter(event.values, acceleroValues);
                acceleroValuesInitialized = true;
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                magneticValues = LowPassFilter.filter(event.values, magneticValues);
                magneticValuesInitialized = true;
                break;
            default:
                // A sensor we're not using, so return
                return;
        }

        if (acceleroValuesInitialized && magneticValuesInitialized) {
            SensorManager.getRotationMatrix(rotationMatrix, null, acceleroValues, magneticValues);
            gotRotationMatrix = true;
        }

        if (gotRotationMatrix) {
            getOrientationValues();

            newHeadingFloat = (float) Math.toDegrees(orientationValues[0]);

            if (this.useTrueNorth && !Float.isNaN(declination)) {
                newHeadingFloat -= declination;
            }

            if (newHeadingFloat < 0) {
                newHeadingFloat += 360;
            }

            newHeadingInt = Math.round(newHeadingFloat);

            if (newHeadingInt != heading.get()) {
                heading.set(newHeadingInt);

                updateListeners();
            }
        }
        computing.set(false);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Ignore
    }

    private void getRotationMatrixFromTruncatedVector(float[] vector) {
        System.arraycopy(vector, 0, truncatedRotationVector, 0, 4);
        SensorManager.getRotationMatrixFromVector(rotationMatrix, truncatedRotationVector);
    }

    private void getOrientationValues() {
        int rot = windowManager.getDefaultDisplay().getRotation();
        float[] matrix;

        switch (rot) {
            case Surface.ROTATION_0:
                matrix = rotationMatrix;
                break;
            case Surface.ROTATION_90:
                SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_Y,
                        SensorManager.AXIS_MINUS_X, remappedMatrix);
                matrix = remappedMatrix;
                break;
            case Surface.ROTATION_180:
                SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_MINUS_X,
                        SensorManager.AXIS_MINUS_Y, remappedMatrix);
                matrix = remappedMatrix;
                break;
            case Surface.ROTATION_270:
                SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_MINUS_Y,
                        SensorManager.AXIS_X, remappedMatrix);
                matrix = remappedMatrix;
                break;
            default:
                // This shouldn't happen - assume default orientation
                matrix = rotationMatrix;
                break;
        }

        SensorManager.getOrientation(matrix, orientationValues);
    }

    @Override
    public void onLocationChanged(Location loc) {
        boolean useSensors = true;
        boolean resetSpeedMatch = true;

        //On an emulator it's about 3x as expensive to calculate declination as it is to calculate distance
        float distance = 0f;

        if (declinationCalculatedAt != null) {
            distance = loc.distanceTo(declinationCalculatedAt);
        }

        if (declinationCalculatedAt == null || distance > CALC_DECLINATION_EVERY_X_METERS) {
            declination = new GeomagneticField((float) loc.getLatitude(), (float) loc.getLongitude(),
                    (float) loc.getAltitude(), loc.getTime()).getDeclination();
            declinationCalculatedAt = loc;
        }

        if (prevLocation != null) {
            if (loc.hasSpeed() && useGpsForHeading) {
                if (UnitsUtil.metersPerSecond2KilometersPerHour(loc.getSpeed()) >= (gpsHeadingMinSpeedKmh)) {
                    gpsHeadingSpeedMatchSeconds += gpsHeadingSpeedMatchSeconds == -1 ? 1 : UnitsUtil.milliSec2Sec(loc.getTime() - prevLocation.getTime());
                    resetSpeedMatch = false;

                    if (gpsHeadingSpeedMatchSeconds >= gpsHeadingMinSpeedSeconds) {
                        if (listeningToSensors) {
                            stopListeningToSensors();
                        }

                        calculateNewHeadingInt(loc);

                        if (newHeadingInt != heading.get()) {
                            heading.set(newHeadingInt);
                            useSensors = false;
                            updateListeners();
                        }
                    }
                }
            }
        }

        if (resetSpeedMatch) {
            gpsHeadingSpeedMatchSeconds = -1;
        }

        if (useSensors && !listeningToSensors) {
            startListeningToSensors();

            if (!listeningToSensors) {
                /* The required sensors are not present */
                calculateNewHeadingInt(loc);

                if (newHeadingInt != heading.get()) {
                    heading.set(newHeadingInt);
                    updateListeners();
                }
            } else {
                Timber.v("Switching from gps to sensors");
            }
        }

        prevLocation = loc;
    }

    private void calculateNewHeadingInt(Location location) {
        newHeadingInt = IntegerDegrees.UNKNOWN;

        if (location.hasBearing()) {
            newHeadingFloat = location.getBearing();

            if (!useTrueNorth && !Float.isNaN(declination)) {
                newHeadingFloat += declination;
            }

            newHeadingInt = Math.round(newHeadingFloat);
        }
    }

    public void addHeadingListener(HeadingListener listener) {
        if (!listeners.contains(listener)) {
            if (listeners.isEmpty()) {
                onResume();
            }

            listeners.add(listener);

            listener.onHeadingChanged(heading);
        }
    }

    public void removeHeadingListener(HeadingListener listener) {
        if (listeners.contains(listener)) {
            listeners.remove(listener);

            if (listeners.isEmpty()) {
                onPause();
            }
        }
    }

    private void updateListeners() {
        for (HeadingListener l : listeners) {
            l.onHeadingChanged(heading);
        }
    }

    @Override
    public void onGpsFixAcquired() {
    }

    @Override
    public void onGpsFixLost() {
        //TODO: If I ever support turn-by-turn navigation, speed en heading should remain at the last know value until gps fix is restored
        Location loc = gpsManager.getLastKnowLocation();

        if (loc == null) {
            return;
        }

        loc.removeBearing();
        loc.removeSpeed();

        onLocationChanged(loc);
    }

    /* This class implements a simple low-pass filter see:
     * http://blog.thomnichols.org/2011/08/smoothing-sensor-data-with-a-low-pass-filter
     */
    private static class LowPassFilter {
        private static final float ALPHA = 0.15f;

        private LowPassFilter() {
        }

        static float[] filter(float[] input, float[] prev) {
            if (input == null || prev == null) {
                throw new NullPointerException("input and prev float arrays must be non-NULL");
            }

            if (input.length != prev.length) {
                throw new IllegalArgumentException("input and prev must be the same length");
            }

            for (int i = 0; i < input.length; i++) {
                prev[i] = prev[i] + ALPHA * (input[i] - prev[i]);
            }
            return prev;
        }
    }
}
