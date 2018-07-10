package nl.erikduisters.pathfinder.data.model.map;

import com.facebook.stetho.okhttp3.StethoInterceptor;

import org.oscim.tiling.TileSource;
import org.oscim.tiling.source.OkHttpEngine;
import org.oscim.tiling.source.oscimap4.OSciMap4TileSource;

import nl.erikduisters.pathfinder.BuildConfig;
import okhttp3.OkHttpClient;

/**
 * Created by Erik Duisters on 07-07-2018.
 */
public enum OnlineMap implements TileSourceProvider {
    OSCIMAP4 {
        @Override
        public TileSource provideTileSource() {
            OkHttpClient.Builder builder;
//TODO: Inject OkHttpClient and use here
            if (BuildConfig.DEBUG) {
                builder = new OkHttpClient.Builder().addNetworkInterceptor(new StethoInterceptor());
            } else {
                builder = new OkHttpClient.Builder();
            }

            return OSciMap4TileSource.builder()
                    .httpFactory(new OkHttpEngine.OkHttpFactory(builder))
                    .build();
        }
    }
    /*,
    NEXTZEN_MVT {
        @Override
        public TileSource provideTileSource() {
            return NextzenMvtTileSource.builder()
                    .apiKey("") // Put a proper API key
                    .httpFactory(new OkHttpEngine.OkHttpFactory())
                    //.locale("en")
                    .build();
        }
    },
    NEXTZEN_GEOJSON {
        @Override
        public TileSource provideTileSource() {
            return NextzenGeojsonTileSource.builder()
                    .apiKey("") // Put a proper API key
                    .httpFactory(new OkHttpEngine.OkHttpFactory())
                    //.locale("en")
                    .build();
        }
    }
    */
}
