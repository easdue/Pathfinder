package nl.erikduisters.pathfinder.service.gpsies_service;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import java.util.List;

import nl.erikduisters.pathfinder.data.model.Marker;

/**
 * Created by Erik Duisters on 13-08-2018.
 */
public interface Result extends Parcelable {
    class NoNetworAvailableError implements Result {
        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
        }

        public NoNetworAvailableError() {
        }

        protected NoNetworAvailableError(Parcel in) {
        }

        public static final Creator<NoNetworAvailableError> CREATOR = new Creator<NoNetworAvailableError>() {
            @Override
            public NoNetworAvailableError createFromParcel(Parcel source) {
                return new NoNetworAvailableError(source);
            }

            @Override
            public NoNetworAvailableError[] newArray(int size) {
                return new NoNetworAvailableError[size];
            }
        };
    }

    class Error implements Result {
        public final @StringRes int messageResId;
        @Nullable public final Object[] formatArgs;
        public final @Nullable Throwable exception;

        public Error(@Nullable Throwable exception, @StringRes int messageResId, @Nullable Object... args) {
            this.exception = exception;
            this.messageResId = messageResId;
            this.formatArgs = args;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.messageResId);
            dest.writeArray(this.formatArgs);
            dest.writeSerializable(this.exception);
        }

        protected Error(Parcel in) {
            this.messageResId = in.readInt();
            this.formatArgs = in.readArray(getClass().getClassLoader());
            this.exception = (Throwable) in.readSerializable();
        }

        public static final Creator<Error> CREATOR = new Creator<Error>() {
            @Override
            public Error createFromParcel(Parcel source) {
                return new Error(source);
            }

            @Override
            public Error[] newArray(int size) {
                return new Error[size];
            }
        };
    }

    class SearchResult implements Result {
        public boolean maxReached;
        public List<Marker> markers;

        public SearchResult(List<Marker> markers, boolean maxReached) {
            this.markers = markers;
            this.maxReached = maxReached;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeByte(this.maxReached ? (byte) 1 : (byte) 0);
            dest.writeTypedList(this.markers);
        }

        protected SearchResult(Parcel in) {
            this.maxReached = in.readByte() != 0;
            this.markers = in.createTypedArrayList(Marker.CREATOR);
        }

        public static final Creator<SearchResult> CREATOR = new Creator<SearchResult>() {
            @Override
            public SearchResult createFromParcel(Parcel source) {
                return new SearchResult(source);
            }

            @Override
            public SearchResult[] newArray(int size) {
                return new SearchResult[size];
            }
        };
    }
}
