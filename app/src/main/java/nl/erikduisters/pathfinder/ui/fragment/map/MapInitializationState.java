package nl.erikduisters.pathfinder.ui.fragment.map;

import android.support.annotation.NonNull;

import org.oscim.core.MapPosition;
import org.oscim.theme.ThemeFile;
import org.oscim.tiling.TileSource;

/**
 * Created by Erik Duisters on 06-07-2018.
 */
public final class MapInitializationState {
    @NonNull final TileSource tileSource;
    @NonNull final MapPosition mapPosition;
    @NonNull final ThemeFile themeFile;
    final boolean addBuildingLayer;
    final boolean addLabelLayer;

    private MapInitializationState(Builder builder) {
        tileSource = builder.tileSource;
        mapPosition = builder.mapPosition;
        themeFile = builder.themeFile;
        addBuildingLayer = builder.addBuildingLayer;
        addLabelLayer = builder.addLabelLayer;
    }

    public static final class Builder {
        private TileSource tileSource;
        private MapPosition mapPosition;
        private ThemeFile themeFile;
        private boolean addBuildingLayer;
        private boolean addLabelLayer;

        public Builder withTileSource(@NonNull TileSource tileSource) {
            this.tileSource = tileSource;

            return this;
        }

        public Builder withMapPosition(@NonNull MapPosition mapPosition) {
            this.mapPosition = mapPosition;

            return this;
        }

        public Builder withTheme(@NonNull ThemeFile themeFile) {
            this.themeFile = themeFile;

            return this;
        }

        public Builder withBuildingLayer() {
            addBuildingLayer = true;

            return this;
        }

        public Builder withLabelLayer() {
            addLabelLayer = true;

            return this;
        }

        public MapInitializationState build() {
            if (tileSource == null || mapPosition == null || themeFile == null) {
                throw new IllegalStateException("TileSource, MapPosition and ThemeFile must be set");
            }

            return new MapInitializationState(this);
        }
    }
}
