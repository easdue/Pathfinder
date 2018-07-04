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

    public boolean hasName() { return name != null; }

    @Nullable
    public String getName() { return name; }

    public void setName(@Nullable String name) { this.name = name; }

    public boolean hasComment() { return comment != null; }

    @Nullable
    public String getComment() { return comment; }

    public void setComment(@Nullable String comment) { this.comment = comment; }

    public boolean hasDescription() { return description != null; }

    @Nullable
    public String getDescription() { return description; }

    public void setDescription(@Nullable String description) { this.description = description; }

    public boolean hasSource() { return source != null; }

    @Nullable
    public String getSource() { return source; }

    public void setSource(@Nullable String source) { this.source = source; }

    public boolean hasNumber() { return number >= 0; }

    public int getNumber() { return number; }

    public void setNumber(int number) { this.number = TypeUtil.assertNonNegativeInteger(number); }

    public boolean hasType() { return type != null; }

    @Nullable
    public String getType() { return type; }

    public void setType(@Nullable String type) { this.type = type; }

    public boolean hasExtensions() { return extensions.size() > 0; }

    @Override
    public List<Gpx.Extension> getExtensions() { return extensions; }

    public boolean hasLinks() { return links.size() > 0; }

    @Override
    public List<Link> getLinks() { return links; }
}
