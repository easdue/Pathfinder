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

package nl.erikduisters.pathfinder.util;

import android.support.annotation.StringRes;
import android.support.v4.app.NotificationManagerCompat;

import nl.erikduisters.pathfinder.R;

/**
 * Created by Erik Duisters on 02-08-2018.
 */
public enum NotificationChannels {
    UNZIPPING_MAPS(R.string.channel_extracting_maps, R.string.channel_extracting_maps_description, NotificationManagerCompat.IMPORTANCE_DEFAULT),
    MAPS_AVAILABLE(R.string.channel_available_maps, R.string.channel_available_maps_description, NotificationManagerCompat.IMPORTANCE_DEFAULT),
    DOWNLOADING_TRACKS(R.string.channel_downloading_tracks, R.string.channel_downloading_tracks_descriptions, NotificationManagerCompat.IMPORTANCE_DEFAULT);

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
