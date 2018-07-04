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