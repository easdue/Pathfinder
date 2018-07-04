package nl.erikduisters.gpx.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by Erik Duisters on 02-07-2018.
 */
public class Link {
    @NonNull private String href;
    @Nullable private String text;
    @Nullable private String type;

    public Link() {
        href = "";
    }

    @NonNull
    public String getHref() {
        return href;
    }

    public void setHref(@NonNull String href) {
        this.href = href;
    }

    public boolean hasText() { return text != null; }

    @Nullable
    public String getText() {
        return text;
    }

    public void setText(@Nullable String text) {
        this.text = text;
    }

    public boolean hasType() { return type != null; }

    @Nullable
    public String getType() {
        return type;
    }

    public void setType(@Nullable String type) {
        this.type = type;
    }
}
