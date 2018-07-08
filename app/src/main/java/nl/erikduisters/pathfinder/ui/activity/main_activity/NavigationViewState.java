package nl.erikduisters.pathfinder.ui.activity.main_activity;

import android.support.annotation.NonNull;

import nl.erikduisters.pathfinder.util.DrawableProvider;
import nl.erikduisters.pathfinder.util.StringProvider;
import nl.erikduisters.pathfinder.util.menu.MyMenu;

/**
 * Created by Erik Duisters on 27-06-2018.
 */
class NavigationViewState {
    @NonNull final DrawableProvider avatar;
    @NonNull final StringProvider userName;
    @NonNull final MyMenu navigationMenu;

    NavigationViewState(@NonNull DrawableProvider avatar,
                        @NonNull StringProvider userName,
                        @NonNull MyMenu navigationMenu) {
        this.avatar = avatar;
        this.userName = userName;
        this.navigationMenu = navigationMenu;
    }
}
