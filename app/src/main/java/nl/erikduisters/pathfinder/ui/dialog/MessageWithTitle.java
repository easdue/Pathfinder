package nl.erikduisters.pathfinder.ui.dialog;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.annotation.VisibleForTesting;

/**
 * Created by Erik Duisters on 14-06-2018.
 */

public class MessageWithTitle implements Parcelable {
    public final int titleResId;
    @VisibleForTesting
    public final int messageResId;
    @Nullable private final String[] args;
    private final String message;

    public MessageWithTitle(@StringRes int titleResId, @StringRes int messageResId, @Nullable String... args) {
        this.titleResId = titleResId;
        this.messageResId = messageResId;
        this.args = args;
        this.message = "";
    }

    public MessageWithTitle(@StringRes int titleResId, @NonNull String message) {
        this.titleResId = titleResId;
        this.messageResId = 0;
        this.args = null;
        this.message = message;
    }

    public String getMessage(Context context) {
        if (messageResId > 0) {
            if (args == null) {
                return context.getString(messageResId);
            } else {
                return context.getString(messageResId, (Object[]) args);
            }
        } else {
            return message;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.titleResId);
        dest.writeInt(this.messageResId);
        dest.writeStringArray(this.args);
        dest.writeString(this.message);
    }

    protected MessageWithTitle(Parcel in) {
        this.titleResId = in.readInt();
        this.messageResId = in.readInt();
        this.args = in.createStringArray();
        this.message = in.readString();
    }

    public static final Creator<MessageWithTitle> CREATOR = new Creator<MessageWithTitle>() {
        @Override
        public MessageWithTitle createFromParcel(Parcel source) {
            return new MessageWithTitle(source);
        }

        @Override
        public MessageWithTitle[] newArray(int size) {
            return new MessageWithTitle[size];
        }
    };
}
