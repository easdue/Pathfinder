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

    @Nullable
    public String getName() { return name; }

    public void setName(@Nullable String name) { this.name = name; }

    @Nullable
    public Email getEmail() { return email; }

    public void setEmail(@Nullable Email email) { this.email = email; }

    @Override
    public List<Link> getLinks() {
        return links;
    }
}
