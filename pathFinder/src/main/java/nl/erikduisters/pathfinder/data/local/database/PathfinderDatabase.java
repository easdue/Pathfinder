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
