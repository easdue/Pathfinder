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

package nl.erikduisters.pathfinder.ui.fragment.compass;

import android.support.annotation.NonNull;

import nl.erikduisters.pathfinder.util.Distance;
import nl.erikduisters.pathfinder.util.IntegerDegrees;
import nl.erikduisters.pathfinder.util.Speed;
import nl.erikduisters.pathfinder.util.menu.MyMenu;

/**
 * Created by Erik Duisters on 28-06-2018.
 */
public final class CompassFragmentViewState {
    @NonNull final MyMenu optionsMenu;
    @NonNull final Speed speed;
    @NonNull final Distance distanceToNext;
    @NonNull final IntegerDegrees heading;
    @NonNull final IntegerDegrees bearing;

    private CompassFragmentViewState(Builder builder) {
        optionsMenu = builder.optionsMenu;
        speed = builder.speed;
        distanceToNext = builder.distanceToNext;
        heading = builder.heading;
        bearing = builder.bearing;
    }

    public static final class Builder {
        private MyMenu optionsMenu;
        private Speed speed;
        private Distance distanceToNext;
        private IntegerDegrees heading;
        private IntegerDegrees bearing;

        public Builder() {
        }

        public Builder withOptionsMenu(@NonNull MyMenu optionsMenu) {
            this.optionsMenu = optionsMenu;
            return this;
        }

        public Builder withSpeed(Speed speed) {
            this.speed = speed;
            return this;
        }

        public Builder withDistanceToNext(Distance distanceToNext) {
            this.distanceToNext = distanceToNext;
            return this;
        }

        public Builder withBearing(@NonNull IntegerDegrees bearing) {
            this.bearing = bearing;
            return this;
        }

        public Builder withHeading(@NonNull IntegerDegrees heading) {
            this.heading = heading;
            return this;
        }

        public CompassFragmentViewState build() {
            if (optionsMenu == null || speed == null || distanceToNext == null || heading == null || bearing == null) {
                throw new IllegalStateException("OptionsMenu, speed, distanceToNext, heading and bearing cannot be null");
            }

            return new CompassFragmentViewState(this);
        }
    }
}
