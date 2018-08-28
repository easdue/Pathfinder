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

package nl.erikduisters.gpx.model;

import android.support.annotation.NonNull;

import nl.erikduisters.gpx.util.TypeUtil;

public class GpsiesMetaDataExtensions implements Gpx.Extension {
    @NonNull private String property;
    private float trackLengthMeter;
    private float totalAscentMeter;
    private float totalDescentMeter;
    private float minHeightMeter;
    private float maxHeightMeter;

    public GpsiesMetaDataExtensions() {
        property = "";
    }

    @NonNull
    public String getProperty() {
        return property;
    }

    public void setProperty(@NonNull String property) { this.property = TypeUtil.assertValidProperty(property); }

    public float getTrackLengthMeter() {
        return trackLengthMeter;
    }

    public void setTrackLengthMeter(float trackLengthMeter) { this.trackLengthMeter = trackLengthMeter; }

    public float getTotalAscentMeter() {
        return totalAscentMeter;
    }

    public void setTotalAscentMeter(float totalAscentMeter) { this.totalAscentMeter = totalAscentMeter; }

    public float getTotalDescentMeter() {
        return totalDescentMeter;
    }

    public void setTotalDescentMeter(float totalDescentMeter) { this.totalDescentMeter = totalDescentMeter; }

    public float getMinHeightMeter() {
        return minHeightMeter;
    }

    public void setMinHeightMeter(float minHeightMeter) {
        this.minHeightMeter = minHeightMeter;
    }

    public float getMaxHeightMeter() {
        return maxHeightMeter;
    }

    public void setMaxHeightMeter(float maxHeightMeter) {
        this.maxHeightMeter = maxHeightMeter;
    }
}