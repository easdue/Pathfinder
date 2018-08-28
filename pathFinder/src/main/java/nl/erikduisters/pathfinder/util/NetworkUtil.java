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
