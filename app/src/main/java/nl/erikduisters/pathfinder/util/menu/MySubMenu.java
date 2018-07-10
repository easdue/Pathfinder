package nl.erikduisters.pathfinder.util.menu;

import android.support.annotation.NonNull;

/**
 * Created by Erik Duisters on 08-07-2018.
 */
public class MySubMenu extends MyMenuItem {
    @NonNull
    private final MyMenu subMenu;
    private final boolean isPopulatedAtRuntime;

    public MySubMenu(int id, boolean enabled, boolean visible, boolean isPopulatedAtRuntime) {
        super(id, enabled, visible);

        this.isPopulatedAtRuntime = isPopulatedAtRuntime;
        subMenu = new MyMenu();
    }

    public void add(MyMenuItem menuItem) {
        if (menuItem instanceof MySubMenu) {
            throw new IllegalArgumentException("You cannot add a submenu to another submenu");
        }

        subMenu.add(menuItem);
    }

    public MyMenu getSubMenu() { return subMenu; }

    @Override
    public boolean isSubMenu() {
        return true;
    }

    public boolean isPopulatedAtRuntime() {
        return isPopulatedAtRuntime;
    }
}
