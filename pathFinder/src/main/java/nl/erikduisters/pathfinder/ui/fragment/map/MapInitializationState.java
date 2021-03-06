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

package nl.erikduisters.pathfinder.ui.fragment.map;

import android.support.annotation.NonNull;
import android.support.annotation.RawRes;
import android.support.annotation.StringRes;

import org.oscim.theme.IRenderTheme;
import org.oscim.tiling.TileSource;

import nl.erikduisters.pathfinder.util.map.ScaleBarType;

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
        final ScaleBarType scaleBarType;
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
            private ScaleBarType scaleBarType;
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

            public boolean hasRenderTheme() {
                return renderTheme != null;
            }
            public IRenderTheme getRenderTheme() { return renderTheme; }

            public Builder withBuildingLayer() {
                addBuildingLayer = true;

                return this;
            }

            public Builder withLabelLayer() {
                addLabelLayer = true;

                return this;
            }

            public Builder withScaleBarType(ScaleBarType scaleBarType) {
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
