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

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Erik Duisters on 29-06-2018.
 */
public class Gpx
        implements ExtensionsContainer,
        WaypointsContainer {
    @IntDef({VERSION.V10, VERSION.V11})
    @Retention(RetentionPolicy.SOURCE)
    public @interface VERSION {
        int V10 = 0;
        int V11 = 1;
    }

    @VERSION private int version;
    @NonNull private String creator;
    @Nullable private Metadata metadata;
    @NonNull private List<Waypoint> waypoints;
    @NonNull private List<Route> routes;
    @NonNull private List<Track> tracks;
    @NonNull private List<Extension> extensions;

    public Gpx() {
        this.creator = "";
        this.waypoints = new ArrayList<>();
        this.routes = new ArrayList<>();
        this.tracks = new ArrayList<>();
        this.extensions = new ArrayList<>();
    }

    public void setVersion(@NonNull String version) throws IllegalArgumentException {
        switch (version) {
            case "1.0":
                this.version = VERSION.V10;
                break;
            case "1.1":
                this.version = VERSION.V11;
                break;
            default:
                throw new IllegalArgumentException("Cannot handle the supplied version");
        }
    }

    @VERSION
    public int getVersion() { return version; }

    @NonNull
    public String getCreator() { return creator; }

    public void setCreator(@NonNull String creator) {
        this.creator = creator;
    }

    public void setMetadata(@NonNull Metadata metadata) {
        this.metadata = metadata;
    }

    public @Nullable Metadata getMetadata() {
        return metadata;
    }

    @NonNull
    @Override
    public List<Waypoint> getWaypoints() {
        return waypoints;
    }

    @NonNull
    public List<Route> getRoutes() {
        return routes;
    }

    @NonNull
    public List<Track> getTracks() {
        return tracks;
    }

    @NonNull
    @Override
    public List<Extension> getExtensions() {
        return extensions;
    }

    public interface Extension {}
}
