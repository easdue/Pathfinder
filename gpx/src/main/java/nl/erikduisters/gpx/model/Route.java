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

package nl.erikduisters.gpx.model;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Erik Duisters on 02-07-2018.
 */
public class Route extends RouteOrTrack implements  WaypointsContainer {
    @NonNull private List<Waypoint> routePoints;

    public Route() {
        super();

        routePoints = new ArrayList<>();
    }

    public List<Waypoint> getRoutePoints() { return routePoints; }

    @Override
    public List<Waypoint> getWaypoints() {
        return routePoints;
    }
}
