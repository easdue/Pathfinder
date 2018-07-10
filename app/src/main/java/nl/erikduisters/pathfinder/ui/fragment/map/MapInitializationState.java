package nl.erikduisters.pathfinder.ui.fragment.map;

import android.support.annotation.NonNull;
import android.support.annotation.RawRes;

import org.oscim.theme.ThemeFile;
import org.oscim.tiling.TileSource;

import nl.erikduisters.pathfinder.data.model.map.ScaleBarType;

/**
 * Created by Erik Duisters on 06-07-2018.
 */
public final class MapInitializationState {
    @NonNull final TileSource tileSource;
    @NonNull final ThemeFile themeFile;
    final boolean addBuildingLayer;
    final boolean addLabelLayer;
    final @ScaleBarType int scaleBarType;
    final boolean addLocationLayer;
    final @RawRes int locationMarkerSvgResId;

    private MapInitializationState(Builder builder) {
        tileSource = builder.tileSource;
        themeFile = builder.themeFile;
        addBuildingLayer = builder.addBuildingLayer;
        addLabelLayer = builder.addLabelLayer;
        scaleBarType = builder.scaleBarType;
        addLocationLayer = builder.addLocationLayer;
        locationMarkerSvgResId = builder.locationMarkerSvgResId;
    }

    public static final class Builder {
        private TileSource tileSource;
        private ThemeFile themeFile;
        private boolean addBuildingLayer;
        private boolean addLabelLayer;
        private @ScaleBarType int scaleBarType;
        private boolean addLocationLayer;
        private @RawRes int locationMarkerSvgResId;

        public Builder withTileSource(@NonNull TileSource tileSource) {
            this.tileSource = tileSource;

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

        public Builder withScaleBarType(@ScaleBarType int scaleBarType) {
            this.scaleBarType = scaleBarType;

            return this;
        }

        public Builder withLocationLayer() {
            this.addLocationLayer = true;

            return this;
        }

        public Builder withLocationMarker(@RawRes int locationMarkerSvgResId) {
            this.locationMarkerSvgResId = locationMarkerSvgResId;

            return this;
        }

        public MapInitializationState build() {
            if (tileSource == null || themeFile == null) {
                throw new IllegalStateException("TileSource and ThemeFile must be set");
            }

            return new MapInitializationState(this);
        }
    }
}
