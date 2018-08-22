package nl.erikduisters.gpx.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by Erik Duisters on 02-07-2018.
 */
public class Copyright {
    @NonNull private String author;
    @Nullable private String year;
    @Nullable private String license;

    public Copyright() {
        author = "";
    }

    @NonNull
    public String getAuthor() {
        return author;
    }

    public void setAuthor(@NonNull String author) {
        this.author = author;
    }

    @Nullable
    public String getYear() {
        return year;
    }

    public void setYear(@Nullable String year) {
        this.year = year;
    }

    @Nullable
    public String getLicense() {
        return license;
    }

    public void setLicense(@Nullable String license) {
        this.license = license;
    }
}
