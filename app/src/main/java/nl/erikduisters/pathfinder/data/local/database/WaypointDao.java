package nl.erikduisters.pathfinder.data.local.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Update;

import nl.erikduisters.pathfinder.data.model.Waypoint;

/**
 * Created by Erik Duisters on 14-06-2018.
 */
@Dao
public interface WaypointDao {
    @Insert
    public void insert(Waypoint waypoint);

    @Delete
    public void delete(Waypoint waypoint);

    @Update
    public void update(Waypoint waypoint);
}
