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

import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.view.Menu;

import nl.erikduisters.pathfinder.util.StringProvider;

/**
 * Created by Erik Duisters on 25-04-2017.
 */

public class MyMenuItem {
    private final @IdRes int id;
    private boolean enabled;
    private boolean visible;
    private boolean checked;
    @Nullable final private StringProvider title;
    @Nullable private Object tag;

    public MyMenuItem(@IdRes int id, boolean enabled, boolean visible) {
        this(id, enabled, visible, false, null);
    }

    public MyMenuItem(boolean enabled, boolean visible, @Nullable StringProvider title) {
        this(Menu.NONE, enabled, visible, false, title);
    }

    public MyMenuItem(@IdRes int id, boolean enabled, boolean visible, boolean isChecked, @Nullable StringProvider title) {
        this.id = id;
        this.enabled = enabled;
        this.visible = visible;
        this.checked = isChecked;
        this.title = title;
    }

    public @IdRes int getId() { return id; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean isEnabled) { enabled = isEnabled; }
    public boolean isVisible() { return visible; }
    public void setVisible(boolean isVisible) { visible = isVisible; }
    public boolean isChecked() { return checked; }
    public void setChecked(boolean isChecked) { checked = isChecked; }

    @Nullable
    public StringProvider getTitle() { return title; }

    @Nullable
    public Object getTag() { return tag; }
    public void setTab(@Nullable Object tag) { this.tag = tag; }

    public boolean isSubMenu() {
        return false;
    }
}
