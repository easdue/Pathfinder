package nl.erikduisters.pathfinder.data.local.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.support.annotation.Nullable;

import nl.erikduisters.pathfinder.data.model.Track;

/**
 * Created by Erik Duisters on 14-06-2018.
 */
@Dao
public interface TrackDao {
    @Insert
    long insert(Track track);

    @Query("SELECT * FROM track WHERE _id = :id")
    @Nullable
    Track getTrackById(long id);

    @Delete
    void delete(Track track);
}
