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

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import java.util.Date;

import static android.arch.persistence.room.ForeignKey.CASCADE;

/**
 * Created by Erik Duisters on 14-06-2018.
 */
@Entity(tableName = "track_point",
        foreignKeys = @ForeignKey(entity = Track.class, parentColumns = "_id",
                                  childColumns = "track_id", onDelete = CASCADE),
        indices = {@Index("track_id")})
public class TrackPoint {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    public long id;
    @ColumnInfo(name = "track_id")
    public long trackId;
    public int segment;
    public double latitude;
    public double longitude;
    public float elevation;
    public Date time;
}
