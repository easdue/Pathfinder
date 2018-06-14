package nl.erikduisters.pathfinder.data.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;

import java.net.URL;

import static android.arch.persistence.room.ForeignKey.CASCADE;

/**
 * Created by Erik Duisters on 14-06-2018.
 */
@Entity(tableName = "waypoint",
        foreignKeys = @ForeignKey(entity = Track.class, parentColumns = "id",
                                  childColumns = "trackId", onDelete = CASCADE))
public class Waypoint {
    @PrimaryKey
    @ColumnInfo(name = "_id")
    public long id;
    public long trackId;
    public double latitude;
    public double longitude;
    public float elevation;
    public String name;
    public String description;
    public URL link;
    public WaypointType type;
}
