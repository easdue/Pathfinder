package nl.erikduisters.pathfinder.data.model.map;

import org.oscim.tiling.TileSource;

import okhttp3.OkHttpClient;

/**
 * Created by Erik Duisters on 07-07-2018.
 */
public interface TileSourceProvider {
    TileSource provideTileSource(OkHttpClient.Builder okHttpClientBuilder);
}
