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
 * Created by Erik Duisters on 30-06-2018.
 */
public class GpsiesWptExtensions implements Gpx.Extension {
    //These are probably from the gpx 1.0 specification. Gpsies uses meterPerSecond instead of speed
    private float course;
    private float speed;

    public GpsiesWptExtensions() {
        course = Float.NaN;
        speed = Float.NaN;
    }

    public boolean hasCourse() { return !Float.isNaN(course); }

    public float getCourse() { return course; }

    public void setCourse(float course) { this.course = TypeUtil.assertValidDegrees(course); }

    public boolean hasSpeed() { return !Float.isNaN(speed); }

    public float getSpeed() { return speed; }

    public void setSpeed(float speed) { this.speed = speed; }
}
