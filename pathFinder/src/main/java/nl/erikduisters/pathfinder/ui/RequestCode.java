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

package nl.erikduisters.pathfinder.ui;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Erik Duisters on 09-06-2018.
 */

@IntDef({RequestCode.REQUEST_PERMISSION})
@Retention(RetentionPolicy.SOURCE)
public @interface RequestCode {
    int REQUEST_PERMISSION = 0;
    int GOOGLEPLAY_ERROR_RESOLUTION_REQUEST = 1;
    int LOCATION_SETTINGS_RESOLUTION_REQUEST = 2;
    int ENABLE_GPS = 3;
}
