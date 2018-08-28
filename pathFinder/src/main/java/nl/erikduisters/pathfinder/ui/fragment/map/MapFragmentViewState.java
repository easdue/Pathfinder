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
import android.support.annotation.Nullable;

import org.oscim.core.MapPosition;

import nl.erikduisters.pathfinder.data.model.FullTrack;
import nl.erikduisters.pathfinder.data.model.MinimalTrackList;
import nl.erikduisters.pathfinder.util.map.LocationLayerInfo;
import nl.erikduisters.pathfinder.util.menu.MyMenu;

/**
 * Created by Erik Duisters on 28-06-2018.
 */
public final class MapFragmentViewState {
    final boolean zoomEnabled;
    final boolean tiltEnabled;
    final boolean rotationEnabled;
    final boolean moveEnabled;
    @NonNull final MyMenu optionsMenu;
    @NonNull final MapPosition mapPosition;
    @NonNull final LocationLayerInfo locationLayerInfo;
    @Nullable final MinimalTrackList minimalTrackList;
    @Nullable final FullTrack fullTrack;

    private MapFragmentViewState(Builder builder) {
        zoomEnabled = builder.zoomEnabled;
        tiltEnabled = builder.tiltEnabled;
        rotationEnabled = builder.rotationEnabled;
        moveEnabled = builder.moveEnabled;
        optionsMenu = builder.optionsMenu;
        mapPosition = builder.mapPosition;
        locationLayerInfo = builder.locationLayerInfo;
        minimalTrackList = builder.minimalTrackList;
        fullTrack = builder.fullTrack;
    }

    public static final class Builder {
        private boolean zoomEnabled;
        private boolean tiltEnabled;
        private boolean rotationEnabled;
        private boolean moveEnabled;
        private MyMenu optionsMenu;
        private MapPosition mapPosition;
        private LocationLayerInfo locationLayerInfo;
        private MinimalTrackList minimalTrackList;
        private FullTrack fullTrack;

        public Builder() {
        }

        public Builder withZoomEnabled(boolean enabled) {
            zoomEnabled = enabled;
            return this;
        }

        public Builder withTiltEnabled(boolean enabled) {
            tiltEnabled = enabled;
            return this;
        }

        public Builder withRotationEnabled(boolean enabled) {
            rotationEnabled = enabled;
            return this;
        }

        public Builder withMoveEnabled(boolean enabled) {
            moveEnabled = enabled;
            return this;
        }

        public Builder withOptionsMenu(@NonNull MyMenu optionsMenu) {
            this.optionsMenu = optionsMenu;
            return this;
        }

        public MyMenu getOptionsMenu() { return optionsMenu; }

        public Builder withMapPosition(@NonNull MapPosition mapPosition) {
            this.mapPosition = mapPosition;
            return this;
        }

        public MapPosition getMapPosition() { return mapPosition; }

        public Builder withLocationLayerInfo(@NonNull LocationLayerInfo info) {
            this.locationLayerInfo = info;
            return this;
        }

        public LocationLayerInfo getLocationLayerInfo() {
            return locationLayerInfo;
        }

        public Builder withMinimalTrackList(@Nullable MinimalTrackList minimalTrackList) {
            this.minimalTrackList = minimalTrackList;
            return this;
        }

        public Builder withFullTrack(@Nullable FullTrack fullTrack) {
            this.fullTrack = fullTrack;
            return this;
        }

        public MapFragmentViewState build() {
            if (optionsMenu == null || mapPosition == null || locationLayerInfo == null) {
                throw new IllegalStateException("optionsMenu, mapPosition and locationLayerInfo cannot be null");
            }
            return new MapFragmentViewState(this);
        }
    }
}
