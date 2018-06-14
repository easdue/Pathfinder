package nl.erikduisters.pathfinder.data.local.database;

import android.arch.persistence.room.TypeConverter;

import java.util.Date;

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
}
