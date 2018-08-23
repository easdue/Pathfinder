package nl.erikduisters.pathfinder.ui.fragment.map;

import android.support.annotation.NonNull;

import org.oscim.core.MapPosition;

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

    private MapFragmentViewState(Builder builder) {
        zoomEnabled = builder.zoomEnabled;
        tiltEnabled = builder.tiltEnabled;
        rotationEnabled = builder.rotationEnabled;
        moveEnabled = builder.moveEnabled;
        optionsMenu = builder.optionsMenu;
        mapPosition = builder.mapPosition;
        locationLayerInfo = builder.locationLayerInfo;
    }

    public static final class Builder {
        private boolean zoomEnabled;
        private boolean tiltEnabled;
        private boolean rotationEnabled;
        private boolean moveEnabled;
        private MyMenu optionsMenu;
        private MapPosition mapPosition;
        private LocationLayerInfo locationLayerInfo;

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

        public MapFragmentViewState build() {
            if (optionsMenu == null || mapPosition == null || locationLayerInfo == null) {
                throw new IllegalStateException("optionsMenu, mapPosition and locationLayerInfo cannot be null");
            }
            return new MapFragmentViewState(this);
        }
    }
}
