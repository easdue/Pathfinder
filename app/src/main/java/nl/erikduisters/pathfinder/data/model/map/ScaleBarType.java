package nl.erikduisters.pathfinder.data.model.map;

import android.content.Context;
import android.support.annotation.StringRes;

import nl.erikduisters.pathfinder.R;

/**
 * Created by Erik Duisters on 10-07-2018.
 */

public enum ScaleBarType {
    NONE(R.string.scale_bar_none),
    METRIC(R.string.scale_bar_metric),
    IMPERIAL(R.string.scale_bar_imperial),
    NAUTICAL(R.string.scale_bar_nautical),
    METRIC_AND_IMPERIAL(R.string.scale_bar_metric_and_imperial);

    private final int stringResId;
    private final static ScaleBarType[] values;

    ScaleBarType(@StringRes int stringResId) {
        this.stringResId = stringResId;
    }

    static {
        values = ScaleBarType.values();
    }

    public static ScaleBarType fromInt(int val) throws IllegalStateException {
        for (ScaleBarType scaleBarType : values()) {
            if (scaleBarType.ordinal() == val) {
                return scaleBarType;
            }
        }

        throw new IllegalStateException("val can not be converted to a ScaleBarType");
    }

    public int toInt() {
        return ordinal();
    }

    public static CharSequence[] getEntries(Context context) {
        String[] entries = new String[values.length];

        int index = 0;
        for (ScaleBarType scaleBarType : values) {
            entries[index] = context.getString(scaleBarType.stringResId);
            index++;
        }

        return entries;
    }

    public static CharSequence[] getEntryValues() {
        String[] entryValues = new String[values.length];

        int index = 0;
        for (ScaleBarType scaleBarType : values) {
            entryValues[index] = scaleBarType.name(); //String.valueOf(scaleBarType.ordinal());
            index++;
        }

        return entryValues;
    }
}

