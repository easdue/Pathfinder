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
public interface TrackPointDao {
    @Insert
    long insert(TrackPoint trackPoint);

    @Query("SELECT * from track_point WHERE _id = :id")
    @Nullable
    TrackPoint getTrackPointByid(long id);

    //TODO: Does this need to be sorted on segment and date? If yes, don't forget to create indexes on those columns
    @Query("SELECT * from track_point WHERE track_id = :trackId")
    @NonNull
    List<TrackPoint> getTrackPointsByTrackId(long trackId);

    @Delete
    void delete(TrackPoint trackPoint);
}
