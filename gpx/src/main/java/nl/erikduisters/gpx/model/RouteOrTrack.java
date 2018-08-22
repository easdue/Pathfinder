package nl.erikduisters.gpx.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import nl.erikduisters.gpx.util.TypeUtil;

/**
 * Created by Erik Duisters on 02-07-2018.
 */
public abstract class RouteOrTrack implements LinksContainer, ExtensionsContainer {
    @Nullable private String name;
    @Nullable private String comment;
    @Nullable private String description;
    @Nullable private String source;
    @NonNull private List<Link> links;
    private int number;
    @Nullable private String type;
    @NonNull public List<Gpx.Extension> extensions;

    RouteOrTrack() {
        links = new ArrayList<>();
        number = -1;
        extensions = new ArrayList<>();
    }

    @Nullable
    public String getName() { return name; }

    public void setName(@Nullable String name) { this.name = name; }

    @Nullable
    public String getComment() { return comment; }

    public void setComment(@Nullable String comment) { this.comment = comment; }

    @Nullable
    public String getDescription() { return description; }

    public void setDescription(@Nullable String description) { this.description = description; }

    @Nullable
    public String getSource() { return source; }

    public void setSource(@Nullable String source) { this.source = source; }

    public int getNumber() { return number; }

    public void setNumber(int number) { this.number = TypeUtil.assertNonNegativeInteger(number); }

    @Nullable
    public String getType() { return type; }

    public void setType(@Nullable String type) { this.type = type; }

    @Override
    public List<Gpx.Extension> getExtensions() { return extensions; }

    @Override
    public List<Link> getLinks() { return links; }
}
