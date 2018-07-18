package nl.erikduisters.pathfinder.ui.fragment.gps_status;

import android.support.annotation.NonNull;

import java.util.List;

import nl.erikduisters.pathfinder.data.local.GpsManager;
import nl.erikduisters.pathfinder.util.Coordinate;
import nl.erikduisters.pathfinder.util.Distance;
import nl.erikduisters.pathfinder.util.IntegerDegrees;
import nl.erikduisters.pathfinder.util.Speed;

/**
 * Created by Erik Duisters on 17-07-2018.
 */
public interface GpsStatusFragmentViewState {
    final class GpsNotEnabledState implements GpsStatusFragmentViewState {}

    final class ShowEnableGpsSettingState implements GpsStatusFragmentViewState {}
    class WaitingForGpsToBeEnabledState implements GpsStatusFragmentViewState {}

    final class DataState implements GpsStatusFragmentViewState {
        @NonNull final List<GpsManager.SatelliteInfo> satInfoList;
        @NonNull final String time;
        @NonNull final Coordinate coordinate;
        @NonNull final Distance accuracy;
        @NonNull final Distance altitude;
        @NonNull final IntegerDegrees heading;
        @NonNull final Speed speed;

        private DataState(Builder builder) {
            satInfoList = builder.satInfoList;
            time = builder.time;
            coordinate = builder.coordinate;
            accuracy = builder.accuracy;
            altitude = builder.altitude;
            heading = builder.heading;
            speed = builder.speed;
        }

        public static final class Builder {
            private List<GpsManager.SatelliteInfo> satInfoList;
            private String time;
            private Coordinate coordinate;
            private Distance accuracy;
            private Distance altitude;
            private IntegerDegrees heading;
            private Speed speed;

            public Builder() {
            }

            public Builder withSatInfoList(@NonNull List<GpsManager.SatelliteInfo> satInfoList) {
                this.satInfoList = satInfoList;
                return this;
            }

            public Builder withTime(@NonNull String time) {
                this.time = time;
                return this;
            }

            public Builder withCoordinate(@NonNull Coordinate coordinate) {
                this.coordinate = coordinate;
                return this;
            }

            public Builder withAccuracy(@NonNull Distance accuracy) {
                this.accuracy = accuracy;
                return this;
            }

            public Builder withAltitude(@NonNull Distance altitude) {
                this.altitude = altitude;
                return this;
            }

            public Builder withHeading(@NonNull IntegerDegrees heading) {
                this.heading = heading;
                return this;
            }

            public Builder withSpeed(@NonNull Speed speed) {
                this.speed = speed;
                return this;
            }

            public DataState build() {
                if (satInfoList == null || time == null || coordinate == null || accuracy == null ||
                        altitude == null || heading == null || speed == null) {
                    throw new IllegalStateException("All fields must be set");
                }
                return new DataState(this);
            }
        }
    }
}
