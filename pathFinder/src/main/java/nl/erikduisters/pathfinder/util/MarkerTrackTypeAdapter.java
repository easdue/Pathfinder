package nl.erikduisters.pathfinder.util;

import com.squareup.moshi.FromJson;
import com.squareup.moshi.ToJson;

import nl.erikduisters.pathfinder.data.model.TrackType;

/**
 * Created by Erik Duisters on 21-08-2018.
 */
public class MarkerTrackTypeAdapter {
    @ToJson
    int toJson(TrackType trackType) {
        return trackType.getMarkersProperty();
    }

    @FromJson
    TrackType fromJson(int markersProperty) {
        return TrackType.fromMarkersProperty(markersProperty);
    }
}
