package nl.erikduisters.pathfinder.ui.fragment.map;

import android.support.annotation.NonNull;
import android.support.annotation.RawRes;
import android.support.annotation.StringRes;

import org.oscim.theme.IRenderTheme;
import org.oscim.tiling.TileSource;

import nl.erikduisters.pathfinder.data.model.map.ScaleBarType;

/**
 * Created by Erik Duisters on 06-07-2018.
 */

public interface MapInitializationState {
    final class MapInitializingState implements MapInitializationState {
        final @StringRes int progressMessageResId;

        MapInitializingState(@StringRes int progressMessageResId) {
            this.progressMessageResId = progressMessageResId;
        }
    }

    final class MapInitializedState implements MapInitializationState {
        @NonNull final TileSource tileSource;
        @NonNull final IRenderTheme renderTheme;
        final boolean addBuildingLayer;
        final boolean addLabelLayer;
        final @ScaleBarType int scaleBarType;
        final boolean addLocationLayer;
        final @RawRes int locationFixedMarkerSvgResId;
        final @RawRes int locationNotFixedMarkerSvgResId;

        private MapInitializedState(Builder builder) {
            tileSource = builder.tileSource;
            renderTheme = builder.renderTheme;
            addBuildingLayer = builder.addBuildingLayer;
            addLabelLayer = builder.addLabelLayer;
            scaleBarType = builder.scaleBarType;
            addLocationLayer = builder.addLocationLayer;
            locationFixedMarkerSvgResId = builder.locationFixedMarkerSvgResId;
            locationNotFixedMarkerSvgResId = builder.locationNotFixedMarkerSvgResId;
        }

        public static final class Builder {
            private TileSource tileSource;
            private IRenderTheme renderTheme;
            private boolean addBuildingLayer;
            private boolean addLabelLayer;
            private @ScaleBarType int scaleBarType;
            private boolean addLocationLayer;
            private @RawRes int locationFixedMarkerSvgResId;
            private @RawRes int locationNotFixedMarkerSvgResId;

            public Builder withTileSource(@NonNull TileSource tileSource) {
                this.tileSource = tileSource;

                return this;
            }

            public Builder withRenderTheme(@NonNull IRenderTheme renderTheme) {
                this.renderTheme = renderTheme;

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

            public Builder withLocationFixedMarker(@RawRes int locationMarkerSvgResId) {
                this.locationFixedMarkerSvgResId = locationMarkerSvgResId;

                return this;
            }

            public Builder withLocationNotFixedMarker(@RawRes int locationMarkerSvgResId) {
                this.locationNotFixedMarkerSvgResId = locationMarkerSvgResId;

                return this;
            }

            public MapInitializedState build() {
                if (tileSource == null || renderTheme == null) {
                    throw new IllegalStateException("TileSource and ThemeFile must be set");
                }

                return new MapInitializedState(this);
            }
        }
    }
}
