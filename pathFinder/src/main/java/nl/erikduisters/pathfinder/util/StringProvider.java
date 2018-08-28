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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

/**
 * Created by Erik Duisters on 27-06-2018.
 */
public class StringProvider {
    @StringRes private int stringResId;
    @Nullable private String string;

    public StringProvider(@StringRes int stringResId) {
        this(stringResId, null);
    }

    public StringProvider(@NonNull String string) {
        this(0, string);
    }

    private StringProvider(@StringRes int stringResId, @Nullable String string) {
        this.stringResId = stringResId;
        this.string = string;
    }

    @NonNull public String getString(@NonNull Context context) {
        if (stringResId != 0) {
            return context.getString(stringResId);
        } else {
            return string == null ? "" : string;
        }
    }
}
