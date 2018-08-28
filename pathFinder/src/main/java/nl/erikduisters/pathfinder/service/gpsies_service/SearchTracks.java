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

package nl.erikduisters.pathfinder.service.gpsies_service;

import android.os.Parcel;
import android.support.annotation.NonNull;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import nl.erikduisters.pathfinder.R;
import nl.erikduisters.pathfinder.data.model.Marker;
import nl.erikduisters.pathfinder.data.model.TrackActivityType;
import nl.erikduisters.pathfinder.data.model.TrackType;
import nl.erikduisters.pathfinder.util.BoundingBox;
import nl.erikduisters.pathfinder.util.MarkerTrackTypeAdapter;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import timber.log.Timber;

/**
 * Created by Erik Duisters on 12-08-2018.
 */
public class SearchTracks extends Job<SearchTracks.JobInfo> {
    private final static String MARKERS_ARRAY_REGEX = "(?s).*markersArray *= *(\\[\\{.*\\}\\]).*";

    SearchTracks(@NonNull JobInfo jobInfo) {
        super(jobInfo);
    }

    @Override
    void execute(OkHttpClient okHttpClient, Job.Callback callback) {
        Request request = new Request.Builder()
                .url(GPSiesService.GPSIES_URL + "/trackList.do")
                .addHeader("Accept-Language", "en-US")
                .post(createRequestBody())
                .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                ResponseBody responseBody = response.body();

                //noinspection ConstantConditions
                String body = responseBody.string();

                boolean maxReached = body.contains("There were too many tracks found");

                String markersArray = "[]";

                if (body.matches(MARKERS_ARRAY_REGEX)) {
                    markersArray = body.replaceAll(MARKERS_ARRAY_REGEX, "$1");
                }

                Moshi moshi = new Moshi.Builder()
                        .add(new MarkerTrackTypeAdapter())
                        .build();

                Type type = Types.newParameterizedType(List.class, Marker.class);
                JsonAdapter<List<Marker>> jsonMarkerAdapter = moshi.adapter(type);

                List<Marker> markers;

                try {
                    markers = jsonMarkerAdapter.fromJson(markersArray);
                    callback.onResult(new Result.SearchResult(markers, maxReached));
                } catch (IOException e) {
                    Timber.d("Cannot parse response");
                    callback.onResult(new Result.Error(e, R.string.cannot_parse_response_from_gpsies));
                }
            } else {
                Timber.d("Response is not successfull: %d - %s", response.code(), response.message());
                callback.onResult(new Result.Error(null, R.string.gpsies_returned, response.code(), response.message()));
            }
        } catch (IOException e) {
            Timber.d("Connecting to GPSies.com failed: %s", e.getMessage());
            callback.onResult(new Result.Error(null, R.string.connecting_to_gpsies_failed, e.getMessage()));
        }
    }

    private RequestBody createRequestBody() {
        FormBody.Builder builder = new FormBody.Builder();

        builder
                .add("search", "true")
                .add("viewSearchResultsInMap", "true")
                .add("searchViewport", "true")
                .add("bbox", boundingBoxToString(jobInfo.boundingBox))
                .add("minLength", String.valueOf(jobInfo.minTrackLength))
                .add("maxLength", String.valueOf(jobInfo.maxTrackLength));

        addTrackActivityTypes(builder);
        addProperty(builder);

        return builder.build();
    }

    private void addTrackActivityTypes(FormBody.Builder builder) {
        for (TrackActivityType activityType : jobInfo.trackActivityTypes) {
            builder.add("trackTypes", activityType.getGPSiesName());
        }
    }

    private void addProperty(FormBody.Builder builder) {
        if (jobInfo.trackTypes.size() == 2) {
            builder.add("property", "both");
        } else if (jobInfo.trackTypes.get(0) == TrackType.ONE_WAY) {
            builder.add("property", "oneWayTrip");
        } else {
            builder.add("property", "roundTrip");
        }
    }

    private String boundingBoxToString(BoundingBox boundingBox) {
        StringBuilder stringBuilder = new StringBuilder();

        return stringBuilder
                .append(boundingBox.minLongitude)
                .append(',')
                .append(boundingBox.minLatitude)
                .append(',')
                .append(boundingBox.maxLongitude)
                .append(',')
                .append(boundingBox.maxLatitude)
                .toString();
    }

    public static class JobInfo implements Job.JobInfo {
        @NonNull BoundingBox boundingBox;
        //final int maxTracks;
        @NonNull final List<TrackActivityType> trackActivityTypes;
        @NonNull final List<TrackType> trackTypes;
        final int minTrackLength;
        final int maxTrackLength;

        private JobInfo(JobInfo.Builder builder) {
            this.boundingBox = builder.boundingBox;
            //this.maxTracks = builder.maxTracks;
            this.trackActivityTypes = builder.trackActivityTypes;
            this.trackTypes = builder.trackTypes;
            this.minTrackLength = builder.minTrackLength;
            this.maxTrackLength = builder.maxTrackLength;
        }

        public static class Builder {
            private BoundingBox boundingBox;
            //private int maxTracks;
            private List<TrackActivityType> trackActivityTypes;
            private List<TrackType> trackTypes;
            private int minTrackLength;
            private int maxTrackLength;

            public Builder withBoundingBox(@NonNull BoundingBox boundingBox) {
                this.boundingBox = boundingBox;
                return this;
            }

            /*
            public Builder withMaxTracks(int maxTracks) {
                this.maxTracks = maxTracks;
                return this;
            }
            */

            public Builder withTrackActivityTypes(@NonNull List<TrackActivityType> trackActivityTypes) {
                this.trackActivityTypes = trackActivityTypes;
                return this;
            }

            public Builder withTrackTypes(@NonNull List<TrackType> trackTypes) {
                this.trackTypes = trackTypes;
                return this;
            }

            public Builder withMinTrackLength(int minTrackLength) {
                this.minTrackLength = minTrackLength;
                return this;
            }

            public Builder withMaxTrackLength(int maxTrackLength) {
                this.maxTrackLength = maxTrackLength;
                return this;
            }

            public JobInfo build() {
                if (boundingBox == null || trackActivityTypes == null || trackTypes == null) {
                    throw new IllegalArgumentException("boundingBox, trackActivityTypes and trackTypes cannot be null");
                }

                return new JobInfo(this);
            }
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeParcelable(this.boundingBox, flags);
            //dest.writeInt(this.maxTracks);
            dest.writeList(this.trackActivityTypes);
            dest.writeList(this.trackTypes);
            dest.writeInt(this.minTrackLength);
            dest.writeInt(this.maxTrackLength);
        }

        protected JobInfo(Parcel in) {
            this.boundingBox = in.readParcelable(BoundingBox.class.getClassLoader());
            //this.maxTracks = in.readInt();
            this.trackActivityTypes = new ArrayList<>();
            in.readList(this.trackActivityTypes, TrackActivityType.class.getClassLoader());
            this.trackTypes = new ArrayList<>();
            in.readList(this.trackTypes, TrackType.class.getClassLoader());
            this.minTrackLength = in.readInt();
            this.maxTrackLength = in.readInt();
        }

        public static final Creator<JobInfo> CREATOR = new Creator<JobInfo>() {
            @Override
            public JobInfo createFromParcel(Parcel source) {
                return new JobInfo(source);
            }

            @Override
            public JobInfo[] newArray(int size) {
                return new JobInfo[size];
            }
        };
    }
}
