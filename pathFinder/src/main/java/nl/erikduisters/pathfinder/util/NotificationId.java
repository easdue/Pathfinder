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

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Erik Duisters on 02-08-2018.
 */

@IntDef({NotificationId.MAP_DOWNLOAD_SERVICE_RUNNING_IN_FOREGROUND, NotificationId.MAP_AVAILABLE, NotificationId.DOWNLOADING_TRACKS})
@Retention(RetentionPolicy.SOURCE)
public @interface NotificationId {
    int MAP_DOWNLOAD_SERVICE_RUNNING_IN_FOREGROUND = 1000;
    int MAP_AVAILABLE = 1001;
    int DOWNLOADING_TRACKS = 1002;
}
