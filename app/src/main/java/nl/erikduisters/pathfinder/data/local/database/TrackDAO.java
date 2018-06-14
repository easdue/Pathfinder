package nl.erikduisters.pathfinder.data.local.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Update;

import nl.erikduisters.pathfinder.data.model.Track;

/**
 * Created by Erik Duisters on 14-06-2018.
 */
@Dao
public interface TrackDAO {
    @Insert
    void insert(Track track);

    @Update
    void update(Track track);

    @Delete
    void delete(Track track);
}
