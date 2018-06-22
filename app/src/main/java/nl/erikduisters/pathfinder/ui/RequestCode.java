package nl.erikduisters.pathfinder.ui;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Erik Duisters on 09-06-2018.
 */

@IntDef({RequestCode.REQUEST_PERMISSION})
@Retention(RetentionPolicy.SOURCE)
public @interface RequestCode {
    int REQUEST_PERMISSION = 0;
    int GOOGLEPLAY_ERROR_RESOLUTION_REQUEST = 1;
}
