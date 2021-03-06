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

package nl.erikduisters.pathfinder.ui.fragment.init_storage;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.StringRes;

import java.io.File;
import java.text.DecimalFormat;

/**
 * Created by Erik Duisters on 08-04-2017.
 */
public class Storage implements Parcelable {
    private static final long MEGABYTE = 1000*1000;
    private static final long GIGABYTE = 1000*1000*1000;


    private final @StringRes int nameResId;
    private final int sequenceNr;
    private final File fileDir;
    private final File cacheDir;

    public Storage(@StringRes int nameResId, int sequenceNr, File fileDir, File cacheDir) {
        this.nameResId = nameResId;
        this.sequenceNr = sequenceNr;
        this.fileDir = fileDir;
        this.cacheDir = cacheDir;
    }

    private String formatSize(long sizeInBytes) {
        if (sizeInBytes > GIGABYTE) {
            return new DecimalFormat("#,###.0GB").format((double)sizeInBytes/GIGABYTE);
        } else {
            return new DecimalFormat("###MB").format(sizeInBytes/MEGABYTE);
        }
    }

    public @StringRes int getNameResId() {
        return nameResId;
    }

    public int getSequenceNr() {
        return sequenceNr;
    }

    public File getFileDir() {
        return fileDir;
    }

    public File getCacheDir() {
        return cacheDir;
    }

    public String getFreeSpace() {
        return formatSize(fileDir.getUsableSpace());
    }

    public String getTotalSpace() {
        return formatSize(fileDir.getTotalSpace());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(nameResId);
        dest.writeInt(sequenceNr);
        dest.writeSerializable(fileDir);
        dest.writeSerializable(cacheDir);
    }

    protected Storage(Parcel in) {
        nameResId = in.readInt();
        sequenceNr = in.readInt();
        fileDir = (File) in.readSerializable();
        cacheDir = (File) in.readSerializable();
    }

    public static final Creator<Storage> CREATOR = new Creator<Storage>() {
        @Override
        public Storage createFromParcel(Parcel source) {
            return new Storage(source);
        }

        @Override
        public Storage[] newArray(int size) {
            return new Storage[size];
        }
    };
}