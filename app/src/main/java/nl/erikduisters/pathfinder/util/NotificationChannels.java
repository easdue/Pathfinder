package nl.erikduisters.pathfinder.util;

import android.support.annotation.StringRes;
import android.support.v4.app.NotificationManagerCompat;

import nl.erikduisters.pathfinder.R;

/**
 * Created by Erik Duisters on 02-08-2018.
 */
public enum NotificationChannels {
    UNZIPPING_MAPS(R.string.channel_extracting_maps, R.string.channel_extracting_maps_description, NotificationManagerCompat.IMPORTANCE_DEFAULT),
    MAPS_AVAILABLE(R.string.channel_available_maps, R.string.channel_available_maps_description, NotificationManagerCompat.IMPORTANCE_DEFAULT);

    private final @StringRes int channelNameResId;
    private final @StringRes int channelDescriptionResId;
    private final int importance;

    NotificationChannels(@StringRes int channelNameResId, @StringRes int channelDescriptionResId, int importance) {
        this.channelNameResId = channelNameResId;
        this.channelDescriptionResId = channelDescriptionResId;
        this.importance = importance;
    }

    public String getChannelId() { return name(); }
    public @StringRes int getChannelNameResId() { return channelNameResId; }
    public @StringRes int getChannelDescriptionResId() { return channelDescriptionResId; }
    public int getImportance() { return importance; }
}
