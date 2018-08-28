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

/**
 * Created by Erik Duisters on 15-07-2018.
 */
public class IntegerDegrees {
    public static final String UNKNOWN_STRING = "---";
    public static final int UNKNOWN = -1;

    private int degrees;

    public IntegerDegrees() {
        degrees = UNKNOWN;
    }

    public IntegerDegrees(int degrees) {
        this.degrees = degrees;
    }

    public int get() {
        return degrees;
    }

    public void set(int degrees) {
        this.degrees = degrees;
    }

    public boolean isUnknown() { return degrees == UNKNOWN; }

    public String asString() {
        if (degrees == UNKNOWN) {
            return UNKNOWN_STRING;
        } else {
            return String.valueOf(degrees);
        }
    }
}
