package nl.erikduisters.pathfinder.util.menu;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;

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

    public void updateAndroidMenu(Menu menu, Context context) {
        for (MyMenuItem myMenuItem : menuItems) {
            MenuItem menuItem = myMenuItem.getId() == Menu.NONE ? null : menu.findItem(myMenuItem.getId());

            if (menuItem != null) {
                update(menuItem, myMenuItem);

                if (myMenuItem.isSubMenu()) {
                    if (menuItem.hasSubMenu()) {
                        SubMenu subMenu = menuItem.getSubMenu();

                        if (((MySubMenu) myMenuItem).isPopulatedAtRuntime()) {
                            subMenu.clear();
                        }

                        ((MySubMenu) myMenuItem).getSubMenu().updateAndroidMenu(menuItem.getSubMenu(), context);
                    } else {
                        throw new RuntimeException("The submenu you are trying to update has not been defined in xml");
                    }
                }
            } else {
                if (!(menu instanceof SubMenu)) {
                    throw new RuntimeException("You can only add new MenuItems to a SubMenu");
                }

                if (myMenuItem.getTitle() == null) {
                    throw new RuntimeException("You cannot add MenuItems without a title");
                }

                MenuItem newMenuItem = menu.add(Menu.NONE, myMenuItem.getId(), Menu.NONE, myMenuItem.getTitle().getString(context));
                update(newMenuItem, myMenuItem);
            }
        }
    }

    private void update(MenuItem menuItem, MyMenuItem myMenuItem) {
        menuItem.setEnabled(myMenuItem.isEnabled());
        menuItem.setVisible(myMenuItem.isVisible());
        menuItem.setCheckable(myMenuItem.isChecked());
        menuItem.setChecked(myMenuItem.isChecked());
    }

    @Nullable
    public MyMenuItem findItem(MenuItem menuItem, @NonNull Context context) {
        for (MyMenuItem myMenuItem : menuItems) {
            if (menuItem.getItemId() != Menu.NONE && menuItem.getItemId() == myMenuItem.getId()) {
                return myMenuItem;
            } else if (menuItem.getItemId() == Menu.NONE) {
                if (myMenuItem.getTitle() != null && menuItem.getTitle().equals(myMenuItem.getTitle().getString(context))) {
                    return myMenuItem;
                }
            }

            if (myMenuItem.isSubMenu()) {
                MyMenuItem subMenuItem = ((MySubMenu) myMenuItem).getSubMenu().findItem(menuItem, context);
                if (subMenuItem != null) {
                    return subMenuItem;
                }
            }
        }

        return null;
    }

    @Nullable
    public MyMenuItem findItem(@IdRes int id) {
        for (MyMenuItem myMenuItem : menuItems) {
            if (id == myMenuItem.getId()) {
                return myMenuItem;
            }

            if (myMenuItem.isSubMenu()) {
                MyMenuItem subMenuItem = ((MySubMenu) myMenuItem).getSubMenu().findItem(id);
                if (subMenuItem != null) {
                    return subMenuItem;
                }
            }
        }

        return null;
    }
}
