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

package nl.erikduisters.pathfinder.ui.fragment.map;

import android.support.annotation.NonNull;

import org.oscim.core.GeoPoint;
import org.oscim.layers.marker.MarkerInterface;
import org.oscim.layers.marker.MarkerSymbol;

import nl.erikduisters.pathfinder.data.model.MinimalTrack;

/**
 * Created by Erik Duisters on 25-08-2018.
 */
public class MinimalTrackMarker implements MarkerInterface {
    @NonNull private MinimalTrack minimalTrack;
    @NonNull private MarkerSymbol markerSymbol;
    private GeoPoint geoPoint;

    MinimalTrackMarker(MinimalTrack minimalTrack, MarkerSymbol markerSymbol) {
        this.minimalTrack = minimalTrack;
        this.markerSymbol = markerSymbol;
        this.geoPoint = new GeoPoint(minimalTrack.startLatitude, minimalTrack.startLongitude);
    }

    @Override
    public MarkerSymbol getMarker() {
        return markerSymbol;
    }

    @Override
    public GeoPoint getPoint() {
        return geoPoint;
    }

    @NonNull
    public MinimalTrack getMinimalTrack() {
        return minimalTrack;
    }
}
