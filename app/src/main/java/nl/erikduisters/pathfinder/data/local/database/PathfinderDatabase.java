package nl.erikduisters.pathfinder.data.local.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

import nl.erikduisters.pathfinder.data.model.Track;
import nl.erikduisters.pathfinder.data.model.TrackPoint;
import nl.erikduisters.pathfinder.data.model.Waypoint;

/**
 * Created by Erik Duisters on 14-06-2018.
 */
@Database(entities = {Track.class, TrackPoint.class, Waypoint.class}, version = 1 )
@TypeConverters({nl.erikduisters.pathfinder.data.local.database.TypeConverters.class})
public abstract class PathfinderDatabase extends RoomDatabase {
    abstract public TrackDao trackDao();
    abstract public TrackPointDao trackPointDao();
    abstract public WaypointDao waypointDao();
}
