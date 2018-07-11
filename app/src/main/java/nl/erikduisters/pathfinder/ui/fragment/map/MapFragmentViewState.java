package nl.erikduisters.pathfinder.ui.fragment.map;

/**
 * Created by Erik Duisters on 28-06-2018.
 */
public final class MapFragmentViewState {
    final boolean zoomEnabled;
    final boolean tiltEnabled;
    final boolean rotationEnabled;
    final boolean moveEnabled;

    private MapFragmentViewState(Builder builder) {
        zoomEnabled = builder.zoomEnabled;
        tiltEnabled = builder.tiltEnabled;
        rotationEnabled = builder.rotationEnabled;
        moveEnabled = builder.moveEnabled;
    }


    public static final class Builder {
        private boolean zoomEnabled;
        private boolean tiltEnabled;
        private boolean rotationEnabled;
        private boolean moveEnabled;

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

        public MapFragmentViewState build() {
            return new MapFragmentViewState(this);
        }
    }
}
