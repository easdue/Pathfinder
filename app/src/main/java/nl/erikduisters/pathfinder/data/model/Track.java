package nl.erikduisters.pathfinder.data.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Date;

/**
 * Created by Erik Duisters on 14-06-2018.
 */
@Entity(tableName = "track")
public class Track {
    @IntDef({Type.ROUND_TRIP, Type.ONE_WAY})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Type {
        int ROUND_TRIP = 0;
        int ONE_WAY = 1;
    }

    @PrimaryKey
    @ColumnInfo(name = "_id")
    public long id;
    public String name;
    public String description;
    public String author;
    @ColumnInfo(name = "gpsies_id")
    public String gpsiesId;
    @ColumnInfo(name ="data_created")
    public Date dateCreated;
    public @Type int type;
    public int length;
    @ColumnInfo(name = "total_ascent")
    public int totalAscent;
    @ColumnInfo(name = "total_descent")
    public int totalDescent;
    @ColumnInfo(name = "min_height")
    public int minHeight;
    @ColumnInfo(name = "max_height")
    public int maxHeight;
}
