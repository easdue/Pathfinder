package nl.erikduisters.pathfinder.util.menu;

import android.support.annotation.NonNull;

/**
 * Created by Erik Duisters on 08-07-2018.
 */
public class MySubMenu extends MyMenuItem {
    @NonNull
    private final MyMenu subMenu;

    public MySubMenu(int id, boolean enabled, boolean visible) {
        super(id, enabled, visible);

        subMenu = new MyMenu();
    }

    public void add(MyMenuItem menuItem) {
        if (menuItem instanceof MySubMenu) {
            throw new IllegalArgumentException("You cannot add a submenu to another submenu");
        }

        subMenu.add(menuItem);
    }

    public MyMenu getSubMenu() { return subMenu; }
}
