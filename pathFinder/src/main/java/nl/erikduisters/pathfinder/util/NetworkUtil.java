package nl.erikduisters.pathfinder.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;

/**
 * Created by Erik Duisters on 21-08-2018.
 */
public class NetworkUtil {
    private NetworkUtil() {}

    public static boolean isNetworkConnected(@NonNull Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        //noinspection ConstantConditions
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        return activeNetwork != null && activeNetwork.isConnected();
    }
}
