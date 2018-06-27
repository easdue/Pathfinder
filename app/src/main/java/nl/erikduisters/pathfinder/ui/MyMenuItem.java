package nl.erikduisters.pathfinder.ui;

import android.support.annotation.IdRes;

/**
 * Created by Erik Duisters on 25-04-2017.
 */

public class MyMenuItem {
    public final @IdRes int id;
    public boolean enabled;
    public boolean visible;

    public MyMenuItem(@IdRes int id, boolean enabled, boolean visible) {
        this.id = id;
        this.enabled = enabled;
        this.visible = visible;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MyMenuItem that = (MyMenuItem) o;

        if (id != that.id) return false;
        if (enabled != that.enabled) return false;
        return visible == that.visible;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (enabled ? 1 : 0);
        result = 31 * result + (visible ? 1 : 0);
        return result;
    }
}
