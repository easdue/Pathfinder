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

package nl.erikduisters.pathfinder.data.local.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

/**
 * Created by Erik Duisters on 14-06-2018.
 */
@Dao
public interface WaypointDao {
    @Insert
    long insert(Waypoint waypoint);

    @Query("SELECT * FROM waypoint WHERE _id = :id")
    @Nullable
    Waypoint getWaypointById(long id);

    @Query("SELECT * FROM waypoint WHERE track_id = :trackId")
    @NonNull
    List<Waypoint> getWaypointsByTrackId(long trackId);

    @Delete
    void delete(Waypoint waypoint);
}
