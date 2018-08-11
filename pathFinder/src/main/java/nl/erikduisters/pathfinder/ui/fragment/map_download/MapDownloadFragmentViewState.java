package nl.erikduisters.pathfinder.ui.fragment.map_download;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import javax.annotation.Nullable;

import nl.erikduisters.pathfinder.util.StringProvider;

/**
 * Created by Erik Duisters on 26-07-2018.
 */
public interface MapDownloadFragmentViewState {
    final class ShowWebsiteState implements MapDownloadFragmentViewState {
        @NonNull final String url;

        ShowWebsiteState(@NonNull String url) { this.url = url; }
    }

    final class DisplayMessageState implements MapDownloadFragmentViewState {
        @StringRes final int messageResId;
        @Nullable String[] args;

        DisplayMessageState(@StringRes int messageResId) {
            this(messageResId, (String[]) null);
        }

        DisplayMessageState(@StringRes int messageResId, @Nullable String... args) {
            this.messageResId = messageResId;
            this.args = args;
        }

        String getMessage(Context context) {
            if (args == null) {
                return context.getString(messageResId);
            } else {
                return context.getString(messageResId, (Object[]) args);
            }
        }
    }

    final class ScheduleMapDownloadState {
        @NonNull StringProvider title;
        @NonNull StringProvider description;
        @NonNull Uri mapUri;
        @android.support.annotation.Nullable Uri destinationUri;

        private ScheduleMapDownloadState(Builder builder) {
            title = builder.title;
            description = builder.description;
            mapUri = builder.mapUri;
            destinationUri = builder.destinationUri;
        }

        public static final class Builder {
            private StringProvider title;
            private StringProvider description;
            private Uri mapUri;
            private Uri destinationUri;

            public Builder() {
            }

            public Builder withTitle(@NonNull StringProvider title) {
                this.title = title;
                return this;
            }

            public Builder withDescription(StringProvider description) {
                this.description = description;
                return this;
            }

            public Builder withMapUri(Uri mapUri) {
                this.mapUri = mapUri;
                return this;
            }

            public Builder withDestinationUri(Uri destinationUri) {
                this.destinationUri = destinationUri;
                return this;
            }

            public ScheduleMapDownloadState build() {
                if (title == null || description == null || mapUri == null) {
                    throw new IllegalStateException("title, description, and mapUri cannot be null");
                }

                return new ScheduleMapDownloadState(this);
            }
        }
    }
}
