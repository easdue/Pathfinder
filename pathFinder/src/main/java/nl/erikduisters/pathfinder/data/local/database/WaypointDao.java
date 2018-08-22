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
