package nl.erikduisters.pathfinder.ui.dialog;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

/**
 * Created by Erik Duisters on 14-06-2018.
 */

public class MessageWithTitle implements Parcelable {
    public final int titleResId;
    public final int messageResId;
    public final String message;

    public MessageWithTitle(@StringRes int titleResId, @StringRes int messageResId) {
        this.titleResId = titleResId;
        this.messageResId = messageResId;
        this.message = "";
    }

    public MessageWithTitle(@StringRes int titleResId, @NonNull String message) {
        this.titleResId = titleResId;
        this.messageResId = 0;
        this.message = message;
    }

    public static final Creator<MessageWithTitle> CREATOR = new Creator<MessageWithTitle>() {
        @Override
        public MessageWithTitle createFromParcel(Parcel in) {
            return new MessageWithTitle(in);
        }

        @Override
        public MessageWithTitle[] newArray(int size) {
            return new MessageWithTitle[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.titleResId);
        dest.writeInt(this.messageResId);
        dest.writeString(this.message);
    }

    protected MessageWithTitle(Parcel in) {
        this.titleResId = in.readInt();
        this.messageResId = in.readInt();
        this.message = in.readString();
    }
}
