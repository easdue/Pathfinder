/*
 * Copyright (c) 2018 Erik Duisters
 *
 * This file is part of Pathfinder
 *
 * Pathfinder is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Foobar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Pathfinder. If not, see <https://www.gnu.org/licenses/>.
 */

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
