package nl.erikduisters.pathfinder.util;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Erik Duisters on 17-08-2018.
 */
@IntDef({JobId.IMPORT_TRACK_SERVICE_JOB_ID})
@Retention(RetentionPolicy.SOURCE)
public @interface JobId {
    int IMPORT_TRACK_SERVICE_JOB_ID = 1000;
}
