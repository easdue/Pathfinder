package nl.erikduisters.pathfinder.data.local.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.support.annotation.Nullable;

import nl.erikduisters.pathfinder.data.model.TrackPoint;

/**
 * Created by Erik Duisters on 14-06-2018.
 */
@Dao
public interface TrackPointDao {
    @Insert
    long insert(TrackPoint trackPoint);

    @Query("SELECT * from track_point WHERE _id = :id")
    @Nullable
    TrackPoint getTrackPointByid(long id);

    @Delete
    void delete(TrackPoint trackPoint);
}
