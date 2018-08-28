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

package nl.erikduisters.pathfinder.ui.preference;

import android.content.Context;
import android.util.AttributeSet;

import net.xpece.android.support.preference.ListPreference;

import nl.erikduisters.pathfinder.R;

/**
 * Created by Erik Duisters on 24-07-2018.
 */
public class ListPreferenceWithButton extends ListPreference {
    public ListPreferenceWithButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        if (getDialogLayoutResource() == 0) {
            setDialogLayoutResource(R.layout.preference_list_fragment_with_button);
        }
    }

    public ListPreferenceWithButton(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ListPreferenceWithButton(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.dialogPreferenceStyle);
    }

    public ListPreferenceWithButton(Context context) {
        this(context, null);
    }
}
