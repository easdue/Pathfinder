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

package nl.erikduisters.pathfinder.util;

/*
 * Created by Erik Duisters on 14-07-2018.
 */

public class UnitsUtil {
    private static final double ME2YD = 1.09361;
    private static final double KM2MI = 0.621371192;
    private static final double MSEC2SEC = 0.001f;
    private static final double MS2KMH = 3.6f;

    public static double milliSec2Sec(long msec) {
        return msec * MSEC2SEC;
    }

    public static double metersPerSecond2KilometersPerHour(double ms) {
        return ms * MS2KMH;
    }

    public static double kilometers2Miles(double km) { return km * KM2MI; }

    public static double meters2yards(double meters) { return meters * ME2YD; }
}