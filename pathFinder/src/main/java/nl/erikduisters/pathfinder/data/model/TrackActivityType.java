/*
 * Copyright (c) 2018 Erik Duisters
 *
 * This file is part of Pathfinder
 *
 * Pathfinder is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Foobar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Pathfinder. If not, see <https://www.gnu.org/licenses/>.
 */

package nl.erikduisters.pathfinder.data.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;

import nl.erikduisters.pathfinder.R;

/**
 * Created by Erik Duisters on 06-08-2018.
 */

//TODO: Better drawables or perferably svg images
public enum TrackActivityType implements Parcelable {
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
    private static final TrackActivityType[] values;

    static {
        values = TrackActivityType.values();
    }

    TrackActivityType(int code, String gpsiesName, @StringRes int nameResId, @DrawableRes int drawableResId) {
        this.code = code;
        this.gpsiesName = gpsiesName;
        this.nameResId = nameResId;
        this.drawableResId = drawableResId;
    }

    public int code() { return code; }

    public static TrackActivityType fromCode(int code) {
        for (TrackActivityType trackActivityType : values) {
            if (trackActivityType.code == code) {
                return trackActivityType;
            }
        }

        throw new IllegalArgumentException("There is no TrackActivityType with code: " + code);
    }

    public String getGPSiesName() { return gpsiesName; }
    public @StringRes int getNameResId() { return nameResId; }
    public @DrawableRes int getDrawableResId() { return drawableResId; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(code);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<TrackActivityType> CREATOR = new Creator<TrackActivityType>() {
        @Override
        public TrackActivityType createFromParcel(Parcel in) {
            int code = in.readInt();
            return TrackActivityType.fromCode(code);
        }

        @Override
        public TrackActivityType[] newArray(int size) {
            return new TrackActivityType[size];
        }
    };
}
