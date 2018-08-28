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

package nl.erikduisters.pathfinder.data.local.database;

import android.arch.persistence.room.TypeConverter;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import nl.erikduisters.pathfinder.data.model.TrackType;
import nl.erikduisters.pathfinder.data.model.WaypointType;

/**
 * Created by Erik Duisters on 14-06-2018.
 */
public class TypeConverters {
    @TypeConverter
    public static Long dateToLong(Date date) {
        return date == null ? null : date.getTime();
    }

    @TypeConverter
    public static Date longToDate(Long date) {
        return date == null ? null : new Date(date);
    }

    @TypeConverter
    public static String urlToString(URL url) { return url == null ? null : url.toString(); }

    @TypeConverter
    public static URL stringToUrl(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            return null;
        }
    }

    @TypeConverter
    public static int trackTypeToInt(TrackType trackType) { return trackType.code(); }

    @TypeConverter
    public static TrackType intToTrackType(int code) { return TrackType.fromCode(code); }

    @TypeConverter
    public static int waypointTypeToInt(WaypointType waypointType) { return waypointType.code(); }

    @TypeConverter
    public static WaypointType intToWaypointType(int code) { return WaypointType.fromInt(code); }
}
