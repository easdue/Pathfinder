package nl.erikduisters.pathfinder.data.model.map;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Erik Duisters on 10-07-2018.
 */

@IntDef({ScaleBarType.NONE, ScaleBarType.METRIC, ScaleBarType.IMPERIAL, ScaleBarType.NAUTICAL, ScaleBarType.METRIC_AND_IMPERIAL})
@Retention(RetentionPolicy.SOURCE)
public @interface ScaleBarType {
    int NONE = 0;
    int METRIC = 1;
    int IMPERIAL = 2;
    int NAUTICAL = 3;
    int METRIC_AND_IMPERIAL = 4;
}
