package nl.erikduisters.gpx.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Erik Duisters on 02-07-2018.
 */
public class Person implements LinksContainer {
    @Nullable private String name;
    @Nullable private Email email;
    @NonNull private List<Link> links;

    public Person() {
        links = new ArrayList<>();
    }

    public boolean hasName() { return name != null; }

    @Nullable
    public String getName() { return name; }

    public void setName(@Nullable String name) { this.name = name; }

    public boolean hasEmail() { return email != null; }

    @Nullable
    public Email getEmail() { return email; }

    public void setEmail(@Nullable Email email) { this.email = email; }

    public boolean hasLinks() { return links.size() > 0; }

    @Override
    public List<Link> getLinks() {
        return links;
    }
}
