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

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

/**
 * Created by Erik Duisters on 14-06-2018.
 */
@Database(entities = {Track.class, TrackPoint.class, Waypoint.class}, version = 1 )
@TypeConverters({nl.erikduisters.pathfinder.data.local.database.TypeConverters.class})
public abstract class PathfinderDatabase extends RoomDatabase {
    abstract public TrackDao trackDao();
    abstract public TrackPointDao trackPointDao();
    abstract public WaypointDao waypointDao();

    public static PathfinderMigration[] getMigrations() {
        return new PathfinderMigration[] {/*PathfinderDatabase.MIGRATION_1_2*/};
    }

    /*TODO: create an integration test to verify the dialog is shown
    private static PathfinderMigration MIGRATION_1_2 = new PathfinderMigration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            for (int i = 0; i <= 100; i+=25) {
                reportProgress(i, R.string.migration_1_to_2 );

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            database.execSQL("DROP INDEX index_track_point_latitude_longitude");
        }
    };
    */
}
