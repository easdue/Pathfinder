package nl.erikduisters.pathfinder.data.model;

import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;

import nl.erikduisters.pathfinder.R;

/**
 * Created by Erik Duisters on 06-08-2018.
 */

//TODO: Better drawables or perferably svg images
public enum TrackActivityType {
    TREKKING(0, "trekking", R.string.trekking, R.drawable.trekking),
    WALKING(1, "walking", R.string.walking, R.drawable.walking),
    JOGGING(2, "jogging", R.string.jogging, R.drawable.jogging),
    CLIMBING(3, "climbing", R.string.climbing, R.drawable.climbing),
    BIKING(4, "biking", R.string.biking, R.drawable.biking),
    RACINGBIKE(5, "racingbike", R.string.racingbike, R.drawable.racingbike),
    MOUNTAINBIKING(6, "mountainbiking", R.string.mountainbiking, R.drawable.mountainbiking),
    PEDELEC(7, "pedelic", R.string.pedelec, R.drawable.pedelec),
    SKATING(8, "skating", R.string.skating, R.drawable.skating),
    CROSSSKATING(9, "crosskating", R.string.crossskating, R.drawable.crossskating),
    HANDCYCLE(10, "handcycle", R.string.handcycle, R.drawable.handcycle),
    MOTORBIKING(11, "motorbiking", R.string.motorbiking, R.drawable.motorbiking),
    MOTOCROSS(12, "motorcross", R.string.motocross, R.drawable.motocross),
    MOTORHOME(13, "motorhome", R.string.motorhome, R.drawable.motorhome),
    CABRIOLET(14, "cabriolet", R.string.cabriolet, R.drawable.cabriolet),
    CAR(15, "car", R.string.car, R.drawable.car),
    RIDING(16, "riding", R.string.riding, R.drawable.riding),
    COACH(17, "coach", R.string.coach, R.drawable.coach),
    SAILING(18, "sailing", R.string.sailing, R.drawable.sailing),
    BOATING(19, "boating", R.string.boating, R.drawable.boating),
    MOTORBOAT(20, "motorboat", R.string.motorboat, R.drawable.motorboat);
    //SWIMMING(),
    //CANOEING(),
    //SKIINGNORDIC(),
    //SKIINGALPINE(),
    //SKIINGRANDONNEE(),
    //SNOWSHOE(),
    //WINTERSPORTS(),
    //FLYING(),
    //TRAIN(),
    //SIGHTSEEING(),
    //GEOCACHING(),
    //MISCELLANEOUS();

    private int code;
    private String gpsiesName;
    private @StringRes int nameResId;
    private @DrawableRes int drawableResId;

    TrackActivityType(int code, String gpsiesName, @StringRes int nameResId, @DrawableRes int drawableResId) {
        this.code = code;
        this.gpsiesName = gpsiesName;
        this.nameResId = nameResId;
        this.drawableResId = drawableResId;
    }

    public int getCode() { return code; }
    public @StringRes int getNameResId() { return nameResId; }
    public @DrawableRes int getDrawableResId() { return drawableResId; }
}
