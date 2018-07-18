package nl.erikduisters.pathfinder.ui.activity.main_activity;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

/**
 * Created by Erik Duisters on 17-07-2018.
 */
public class StartActivityViewState {
    @NonNull private final Class<?> activityClass;

    StartActivityViewState(@NonNull Class activityClass) {
        this.activityClass = activityClass;
    }

    Intent getIntent(Context context) {
        return new Intent(context, activityClass);
    }
}
