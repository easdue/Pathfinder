package nl.erikduisters.pathfinder.data.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Date;

/**
 * Created by Erik Duisters on 14-06-2018.
 */
@Entity(tableName = "track",
        indices = {@Index("name"), @Index("gpsies_id")})
public class Track {
    @IntDef({Type.ROUND_TRIP, Type.ONE_WAY})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Type {
        int ROUND_TRIP = 1;
        int ONE_WAY = 2;
    }

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    public long id;
    public String name;
    public String description;
    public String author;
    @ColumnInfo(name = "gpsies_id")
    public String gpsiesFileId;
    @ColumnInfo(name ="data_created")
    public Date dateCreated;
    public @Type int type;
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
