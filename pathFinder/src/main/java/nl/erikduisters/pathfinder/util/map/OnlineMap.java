package nl.erikduisters.pathfinder.util.map;

import org.oscim.tiling.TileSource;
import org.oscim.tiling.source.OkHttpEngine;
import org.oscim.tiling.source.bitmap.BitmapTileSource;
import org.oscim.tiling.source.oscimap4.OSciMap4TileSource;

import okhttp3.OkHttpClient;

/**
 * Created by Erik Duisters on 07-07-2018.
 */
public enum OnlineMap implements TileSourceProvider {
    OSCIMAP4 {
        @Override
        public TileSource provideTileSource(OkHttpClient.Builder builder) {
            return OSciMap4TileSource.builder()
                    .httpFactory(new OkHttpEngine.OkHttpFactory(builder))
                    .build();
        }
    },
    HIKEBIKE {
        @Override
        public TileSource provideTileSource(OkHttpClient.Builder builder) {
            return BitmapTileSource.builder()
                    .url("http://tiles.wmflabs.org/hikebike")
                    .tilePath("/{Z}/{X}/{Y}.png")
                    .zoomMax(17)
                    .httpFactory(new OkHttpEngine.OkHttpFactory(builder))
                    .build();
        }
    },
    HIKEBIKE_HILLSHADE {
        @Override
        public TileSource provideTileSource(OkHttpClient.Builder builder) {
            return BitmapTileSource.builder()
                    .url("http://tiles.wmflabs.org/hillshading")
                    .tilePath("/{Z}/{X}/{Y}.png")
                    .zoomMax(14)
                    .httpFactory(new OkHttpEngine.OkHttpFactory(builder))
                    .build();
        }
    }
    /*,
    NEXTZEN_MVT {
        @Override
        public TileSource provideTileSource(OkHttpClient.Builder builder) {
            return NextzenMvtTileSource.builder()
                    .apiKey(BuildConfig.NEXTZEN_API_KEY)
                    .httpFactory(new OkHttpEngine.OkHttpFactory())
                    //.locale("en")
                    .build();
        }
    }*/
}
