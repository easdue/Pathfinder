package nl.erikduisters.pathfinder.data.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import java.util.Date;

import static android.arch.persistence.room.ForeignKey.CASCADE;

/**
 * Created by Erik Duisters on 14-06-2018.
 */
@Entity(tableName = "track_point",
        foreignKeys = @ForeignKey(entity = Track.class, parentColumns = "_id",
                                  childColumns = "track_id", onDelete = CASCADE),
        indices = {@Index("track_id")})
public class TrackPoint {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    public long id;
    @ColumnInfo(name = "track_id")
    public long trackId;
    public int segment;
    public double latitude;
    public double longitude;
    public float elevation;
    public Date time;
}
