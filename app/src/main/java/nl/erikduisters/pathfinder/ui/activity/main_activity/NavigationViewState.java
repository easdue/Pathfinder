package nl.erikduisters.pathfinder.ui.activity.main_activity;

import android.support.annotation.NonNull;

import java.util.List;

import nl.erikduisters.pathfinder.ui.MyMenuItem;
import nl.erikduisters.pathfinder.util.DrawableProvider;
import nl.erikduisters.pathfinder.util.StringProvider;

/**
 * Created by Erik Duisters on 27-06-2018.
 */
class NavigationViewState {
    @NonNull final DrawableProvider avatar;
    @NonNull final StringProvider userName;
    @NonNull final List<MyMenuItem> navigationMenu;

    NavigationViewState(@NonNull DrawableProvider avatar,
                        @NonNull StringProvider userName,
                        @NonNull List<MyMenuItem> navigationMenu) {
        this.avatar = avatar;
        this.userName = userName;
        this.navigationMenu = navigationMenu;
    }
}
