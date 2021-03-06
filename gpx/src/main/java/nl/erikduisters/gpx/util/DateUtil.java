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

package nl.erikduisters.gpx.util;

import android.annotation.SuppressLint;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Erik Duisters on 30-06-2018.
 */
public class DateUtil {
    private final static String TAG = DateUtil.class.getSimpleName();

    @SuppressLint("SimpleDateFormat")
    private final static SimpleDateFormat utcDateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

    private DateUtil() {
        //This is a utility class so cannot be instantiated
    }

    /**
     * Tries to parse the supplied string in "yyyy-MM-dd'T'HH:mm:ssZ"
     * format. ".SSSSSSZ" or "Z" are replaced by "+0000" so SimpleDateFormat
     * can understand UTC
     *
     * @param date The string to parse for a date
     * @return The parsed Date or null
     */
    public static synchronized Date parseXmlDate(String date) throws IllegalArgumentException {
        Date parsedDate;

        /* Replace .SSSSSSZ or Z by "+0000" so SimpleDateFormat understands UTC */
        int i = date.indexOf('.');
        String myDate = date;

        if (i == -1)
            i = date.indexOf('Z');

        if (i >= 0) {
            myDate = date.substring(0, i);
            myDate += "+0000";
        } else {
            myDate += "+0000";
        }

        try {
            parsedDate = utcDateFormatter.parse(myDate);
        } catch (ParseException e) {
            throw new IllegalArgumentException("The provided data cannot be parsed");
        }

        return parsedDate;
    }
}
