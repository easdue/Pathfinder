package nl.erikduisters.pathfinder.ui.fragment.runtime_permission;

/**
 * Created by Erik Duisters on 09-06-2018.
 */

public interface RuntimePermissionHelper {
    boolean hasPermission(String permission);
    boolean shouldShowPermissionRationale(String permission);
}
