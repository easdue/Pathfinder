package nl.erikduisters.pathfinder.data.local.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

import nl.erikduisters.pathfinder.data.model.FullTrack;
import nl.erikduisters.pathfinder.data.model.MinimalTrack;

/**
 * Created by Erik Duisters on 14-06-2018.
 */
@Dao
public interface TrackDao {
    @Insert
    long insert(Track track);

    @Query("SELECT * FROM track WHERE _id = :id")
    @Nullable
    FullTrack getTrackById(long id);

    @Query("SELECT t._id AS id, t.name, t.author, t.type, t.length, t.total_ascent AS totalAscent, t.total_descent AS totalDescent, " +
            "(SELECT COUNT(_id) FROM waypoint wp WHERE wp.track_id = t._id) AS numWaypoints, " +
            "(SELECT COUNT(_id) FROM track_point tp WHERE tp.track_id = t._id) AS numTrackPoints, " +
            "tp2.latitude AS startLatitude, tp2.longitude AS startLongitude FROM track t LEFT JOIN " +
            "(SELECT track_id, time, latitude, longitude from track_point GROUP BY track_id HAVING MIN(time) ORDER BY time) " +
            "AS tp2 ON tp2.track_id = t._id WHERE (tp2.latitude BETWEEN :minLat AND :maxLat AND tp2.longitude BETWEEN :minLon and :maxLon) OR t._id = :trackIdToInclude")
    @NonNull
    LiveData<List<MinimalTrack>> getMinimalTracks(double minLat, double minLon, double maxLat, double maxLon, long trackIdToInclude);

    @Query("DELETE FROM track WHERE gpsies_file_id = :gpsiesFileId")
    void delete(String gpsiesFileId);

    @Delete
    void delete(Track track);
}
