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

package nl.erikduisters.pathfinder.util.map;

import android.location.Location;
import android.support.annotation.Nullable;

/**
 * Created by Erik Duisters on 10-07-2018.
 */

public class LocationLayerInfo {
    public boolean hasFix;
    public double latitude;
    public double longitude;
    public float accuracy;
    public boolean hasBearing;
    public float bearing;

    public LocationLayerInfo(@Nullable Location location) {
        hasFix = true;
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        accuracy = location.getAccuracy();
    }
}
