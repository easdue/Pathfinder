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
