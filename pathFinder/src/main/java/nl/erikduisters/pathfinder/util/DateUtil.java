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

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Erik Duisters on 18-07-2018.
 */
public class DateUtil {
    @SuppressLint("SimpleDateFormat")
    private final static SimpleDateFormat utcTimeFormatter = new SimpleDateFormat("HH:mm:ss");

    private DateUtil() {}

    public static synchronized String utcTime(long milliseconds) {
        StringBuilder time = new StringBuilder();
        Date date = new Date(milliseconds);

        utcTimeFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        time.append(utcTimeFormatter.format(date));
        time.append(" utc");

        return time.toString();
    }
}
