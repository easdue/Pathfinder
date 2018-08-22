package nl.erikduisters.pathfinder.data.local.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import java.util.Date;

import nl.erikduisters.pathfinder.data.model.TrackType;

/**
 * Created by Erik Duisters on 14-06-2018.
 */
@Entity(tableName = "track",
        indices = {@Index("name"), @Index("gpsies_file_id")})
public class Track {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    public long id;
    public String name;
    public String description;
    public String author;
    @ColumnInfo(name = "gpsies_file_id")
    public String gpsiesFileId;
    @ColumnInfo(name ="data_created")
    public Date dateCreated;
    public TrackType type;
    public float length;
    @ColumnInfo(name = "total_ascent")
    public float totalAscent;
    @ColumnInfo(name = "total_descent")
    public float totalDescent;
    @ColumnInfo(name = "min_height")
    public float minHeight;
    @ColumnInfo(name = "max_height")
    public float maxHeight;
}
