package nl.erikduisters.pathfinder.util;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Erik Duisters on 02-08-2018.
 */

@IntDef({NotificationId.MAP_DOWNLOAD_SERVICE_RUNNING_IN_FOREGROUND, NotificationId.MAP_AVAILABLE, NotificationId.DOWNLOADING_TRACKS})
@Retention(RetentionPolicy.SOURCE)
public @interface NotificationId {
    int MAP_DOWNLOAD_SERVICE_RUNNING_IN_FOREGROUND = 1000;
    int MAP_AVAILABLE = 1001;
    int DOWNLOADING_TRACKS = 1002;
}
