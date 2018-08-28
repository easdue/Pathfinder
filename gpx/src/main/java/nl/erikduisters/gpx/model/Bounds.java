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

import nl.erikduisters.gpx.util.TypeUtil;

/**
 * Created by Erik Duisters on 02-07-2018.
 */
public class Bounds {
    private double minLatitude;
    private double minLongitude;
    private double maxLatitude;
    private double maxLongitude;

    public double getMinLatitude() { return minLatitude; }

    public void setMinLatitude(double minLatitude) { this.minLatitude = TypeUtil.assertValidLatitude(minLatitude); }

    public double getMinLongitude() { return minLongitude; }

    public void setMinLongitude(double minLongitude) { this.minLongitude = TypeUtil.assertValidLongitude(minLongitude); }

    public double getMaxLatitude() { return maxLatitude; }

    public void setMaxLatitude(double maxLatitude) { this.maxLatitude = TypeUtil.assertValidLatitude(maxLatitude); }

    public double getMaxLongitude() { return maxLongitude; }

    public void setMaxLongitude(double maxLongitude) { this.maxLongitude = TypeUtil.assertValidLongitude(maxLongitude); }
}
