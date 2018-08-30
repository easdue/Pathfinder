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

package nl.erikduisters.pathfinder.util.map;

import org.oscim.tiling.TileSource;
import org.oscim.tiling.source.OkHttpEngine;
import org.oscim.tiling.source.bitmap.BitmapTileSource;
import org.oscim.tiling.source.mvt.NextzenMvtTileSource;
import org.oscim.tiling.source.oscimap4.OSciMap4TileSource;

import nl.erikduisters.pathfinder.BuildConfig;
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

        @Override
        public boolean isBitmapTileSource() {
            return false;
        }
    },
    NEXTZEN_MVT {
        @Override
        public TileSource provideTileSource(OkHttpClient.Builder builder) {
            return NextzenMvtTileSource.builder()
                    .apiKey(BuildConfig.NEXTZEN_API_KEY)
                    .httpFactory(new OkHttpEngine.OkHttpFactory(builder))
                    //.locale("en")
                    .build();
        }

        @Override
        public boolean isBitmapTileSource() {
            return false;
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

        @Override
        public boolean isBitmapTileSource() {
            return true;
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

        @Override
        public boolean isBitmapTileSource() {
            return true;
        }
    }
}
