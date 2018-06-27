package nl.erikduisters.pathfinder.ui.activity.main_activity;

import android.support.annotation.NonNull;

import java.util.List;

import nl.erikduisters.pathfinder.ui.MyMenuItem;
import nl.erikduisters.pathfinder.util.DrawableType;
import nl.erikduisters.pathfinder.util.StringType;

/**
 * Created by Erik Duisters on 27-06-2018.
 */
class NavigationViewState {
    @NonNull final DrawableType avatar;
    @NonNull final StringType userName;
    @NonNull final List<MyMenuItem> navigationMenu;

    NavigationViewState(@NonNull DrawableType avatar,
                        @NonNull StringType userName,
                        @NonNull List<MyMenuItem> navigationMenu) {
        this.avatar = avatar;
        this.userName = userName;
        this.navigationMenu = navigationMenu;
    }
}
