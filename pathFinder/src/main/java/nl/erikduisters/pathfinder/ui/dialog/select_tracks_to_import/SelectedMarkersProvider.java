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

package nl.erikduisters.pathfinder.ui.dialog.select_tracks_to_import;

import android.support.annotation.Nullable;

import java.util.List;

import nl.erikduisters.pathfinder.data.model.Marker;

/**
 * Created by Erik Duisters on 16-08-2018.
 */
public interface SelectedMarkersProvider {
    interface OnSelectionChangedListener {
        void onSelectionChanged(int numSelected);
    }

    void setOnSelectionChangedListener(@Nullable OnSelectionChangedListener listener);
    List<Marker> getSelectedMarkers();
}
