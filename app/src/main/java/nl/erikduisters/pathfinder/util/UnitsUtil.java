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