package nl.erikduisters.pathfinder.util;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Erik Duisters on 15-07-2018.
 */
@IntDef({Units.METRIC, Units.IMPERIAL})
@Retention(RetentionPolicy.SOURCE)
public @interface Units {
    int METRIC = 0;
    int IMPERIAL = 1;
}
