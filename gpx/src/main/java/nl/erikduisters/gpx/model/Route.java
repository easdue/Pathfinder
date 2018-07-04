package nl.erikduisters.gpx.model;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Erik Duisters on 02-07-2018.
 */
public class Route extends RouteOrTrack implements  WaypointsContainer {
    @NonNull private List<Waypoint> routePoints;

    public Route() {
        super();

        routePoints = new ArrayList<>();
    }

    public boolean hasRoutePoints() { return routePoints.size() > 0; }

    public List<Waypoint> getRoutePoints() { return routePoints; }

    @Override
    public List<Waypoint> getWaypoints() {
        return routePoints;
    }
}
