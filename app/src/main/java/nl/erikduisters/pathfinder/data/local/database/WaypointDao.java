package nl.erikduisters.pathfinder.data.local.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.support.annotation.Nullable;

import nl.erikduisters.pathfinder.data.model.Waypoint;

/**
 * Created by Erik Duisters on 14-06-2018.
 */
@Dao
public interface WaypointDao {
    @Insert
    public long insert(Waypoint waypoint);

    @Query("SELECT * FROM waypoint WHERE _id = :id")
    @Nullable
    public Waypoint getWaypointById(long id);

    @Delete
    public void delete(Waypoint waypoint);
}
