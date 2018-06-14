package nl.erikduisters.pathfinder.data.local.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;

import java.util.List;

import nl.erikduisters.pathfinder.data.model.TrackPoint;

/**
 * Created by Erik Duisters on 14-06-2018.
 */
@Dao
public interface TrackPointDao {
    @Insert
    void insert(TrackPoint trackPoint);

    @Insert
    void insert(List<TrackPoint> trackPoints);
}
