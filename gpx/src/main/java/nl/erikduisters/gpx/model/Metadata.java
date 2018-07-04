package nl.erikduisters.gpx.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Erik Duisters on 02-07-2018.
 */
public class Metadata implements LinksContainer, ExtensionsContainer {
    @Nullable private String name;
    @Nullable private String description;
    @Nullable private Person author;
    @Nullable private Copyright copyright;
    @NonNull private List<Link> links;
    @Nullable private Date time;
    @Nullable private String keywords;
    @Nullable private Bounds bounds;
    @NonNull private List<Gpx.Extension> extensions;

    public Metadata() {
        links = new ArrayList<>();
        extensions = new ArrayList<>();
    }

    public boolean hasName() { return name != null; }

    @Nullable
    public String getName() {
        return name;
    }

    public void setName(@Nullable String name) {
        this.name = name;
    }

    public boolean hasDescription() { return description != null; }

    @Nullable
    public String getDescription() {
        return description;
    }

    public void setDescription(@Nullable String description) {
        this.description = description;
    }

    public boolean hasAuthor() { return author != null; }

    @Nullable
    public Person getAuthor() {
        return author;
    }

    public void setAuthor(@Nullable Person author) {
        this.author = author;
    }

    public boolean hasCopyright() { return copyright != null; }

    @Nullable
    public Copyright getCopyright() {
        return copyright;
    }

    public void setCopyright(@Nullable Copyright copyright) {
        this.copyright = copyright;
    }

    public boolean hasTime() { return time != null; }

    @Nullable
    public Date getTime() {
        return time;
    }

    public void setTime(@Nullable Date time) {
        this.time = time;
    }

    public boolean hasKeywords() { return getKeywords() != null; }

    @Nullable
    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(@Nullable String keywords) {
        this.keywords = keywords;
    }

    public boolean hasBounds() { return bounds != null; }

    @Nullable
    public Bounds getBounds() {
        return bounds;
    }

    public void setBounds(@Nullable Bounds bounds) {
        this.bounds = bounds;
    }

    public boolean hasExtensions() { return extensions.size() > 0; }

    @Override
    public List<Gpx.Extension> getExtensions() {
        return extensions;
    }

    public boolean hasLinks() { return links.size() > 0; }

    @Override
    public List<Link> getLinks() {
        return links;
    }
}
