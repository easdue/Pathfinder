package nl.erikduisters.pathfinder.util.menu;

import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Erik Duisters on 08-07-2018.
 */
public class MyMenu {
    @NonNull
    private final List<MyMenuItem> menuItems;

    public MyMenu() { menuItems = new ArrayList<>(); }

    public void add(MyMenuItem menuItem) { menuItems.add(menuItem); }

    public MySubMenu add(MySubMenu subMenu) {
        menuItems.add(subMenu);

        return subMenu;
    }

    public List<MyMenuItem> getMenuItems() { return menuItems; }

    public void updateAndroidMenu(Menu menu) {
        for (MyMenuItem myMenuItem : menuItems) {
            MenuItem menuItem = menu.findItem(myMenuItem.id);

            if (menuItem != null) {
                menuItem.setEnabled(myMenuItem.enabled);
                menuItem.setVisible(myMenuItem.visible);
            }

            if (myMenuItem instanceof MySubMenu) {
                ((MySubMenu) myMenuItem).getSubMenu().updateAndroidMenu(menu);
            }
        }
    }
}
