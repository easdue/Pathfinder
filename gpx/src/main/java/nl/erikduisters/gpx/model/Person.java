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
