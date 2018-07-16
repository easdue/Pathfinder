package nl.erikduisters.pathfinder.util;

/**
 * Created by Erik Duisters on 15-07-2018.
 */
public class IntegerDegrees {
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
}
