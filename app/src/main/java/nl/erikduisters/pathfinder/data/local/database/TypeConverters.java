package nl.erikduisters.pathfinder.data.local.database;

import android.arch.persistence.room.TypeConverter;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import nl.erikduisters.pathfinder.data.model.WaypointType;

/**
 * Created by Erik Duisters on 14-06-2018.
 */
public class TypeConverters {
    @TypeConverter
    public static Long dateToLong(Date date) {
        return date.getTime();
    }

    @TypeConverter
    public static Date longToDate(Long date) {
        return new Date(date);
    }

    @TypeConverter
    public static String urlToString(URL url) { return url.toString(); }

    @TypeConverter
    public static URL stringToUrl(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            return null;
        }
    }

    @TypeConverter
    public static int waypointTypeToInt(WaypointType waypointType) { return waypointType.code(); }

    @TypeConverter
    public static WaypointType intToWaypointType(int code) { return WaypointType.fromInt(code); }
}
