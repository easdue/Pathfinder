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

import android.arch.lifecycle.LiveData;
import android.location.Location;
import android.support.annotation.NonNull;

import org.oscim.core.MapPosition;

import javax.inject.Inject;
import javax.inject.Singleton;

import nl.erikduisters.pathfinder.data.model.MinimalTrackList;
import nl.erikduisters.pathfinder.util.SingleSourceMediatorLiveData;
import timber.log.Timber;

/**
 * Created by Erik Duisters on 23-08-2018.
 */
//TODO: MapFragmentViewModel must save MapPosition and BoundingBox to preferences as soon as the user changes the map position otherwise I won't be able to reload using the new center position
@Singleton
public class MinimalTrackListLoader implements GpsManager.LocationListener, SingleSourceMediatorLiveData.Listener {
    private final GpsManager gpsManager;
    private final TrackRepository trackRepository;
    private final PreferenceManager preferenceManager;
    private SingleSourceMediatorLiveData<MinimalTrackList> minimalTrackListObservable;
    private long idOfTrackToInclude;
    private Location currentlyLoadingLocation;

    @Inject
    MinimalTrackListLoader(GpsManager gpsManager, TrackRepository trackRepository, PreferenceManager preferenceManager) {
        this.gpsManager = gpsManager;
        this.trackRepository = trackRepository;
        this.preferenceManager = preferenceManager;
        minimalTrackListObservable = new SingleSourceMediatorLiveData<>();
        minimalTrackListObservable.setListener(this);
        idOfTrackToInclude = -1;
    }

    public LiveData<MinimalTrackList> getMinimalTrackListObservable() {
        return minimalTrackListObservable;
    }

    public void loadMinimalTrackList(long idOfTrackToInclude) {
        Location center;

        this.idOfTrackToInclude = idOfTrackToInclude;

        if (!preferenceManager.mapFollowsGps()) {
            MapPosition mapPosition = preferenceManager.getMapPosition();
            center = new Location("");
            center.setLatitude(mapPosition.getLatitude());
            center.setLongitude(mapPosition.getLongitude());
        } else {
            center = gpsManager.getLastKnowLocation();
        }

        if (center == null) {
            return;
        }

        loadMinimalTrackList(center, idOfTrackToInclude);
    }

    private void loadMinimalTrackList(Location center, long idOfTrackToInclude) {
        currentlyLoadingLocation = center;

        LiveData<MinimalTrackList> liveData = trackRepository.getMinimalTrackList(center, preferenceManager.getTrackLoadRadius(), idOfTrackToInclude);
        minimalTrackListObservable.addSource(liveData, value -> { minimalTrackListObservable.setValue(value); currentlyLoadingLocation = null; });
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        if (!preferenceManager.mapFollowsGps()) {
            return;
        }

        reloadIfRequired(location);
    }

    public void onMapLocationChanged(@NonNull Location location) {
        reloadIfRequired(location);
    }

    private void reloadIfRequired(Location location) {
        if (minimalTrackListObservable.getValue() == null) {
            return;
        }

        Location lastCenterLocation = currentlyLoadingLocation != null ? currentlyLoadingLocation : minimalTrackListObservable.getValue().getCenter();

        if (lastCenterLocation.distanceTo(location) >= 1000f) {
            Timber.d("Location changed more than 1000 meters, reloading minimalCacheList");
            loadMinimalTrackList(location, idOfTrackToInclude);
        }
    }

    @Override
    public void onActive() {
        Timber.d("onActive()");
        gpsManager.addLocationListener(this);
    }

    @Override
    public void onInactive() {
        Timber.d("onInactive()");
        gpsManager.removeLocationListener(this);
    }
}
