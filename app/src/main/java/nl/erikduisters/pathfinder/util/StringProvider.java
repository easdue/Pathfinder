package nl.erikduisters.pathfinder.util;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

/**
 * Created by Erik Duisters on 27-06-2018.
 */
public class StringProvider {
    @StringRes private int stringResId;
    @Nullable private String string;

    public StringProvider(@StringRes int stringResId) {
        this(stringResId, null);
    }

    public StringProvider(@NonNull String string) {
        this(0, string);
    }

    private StringProvider(@StringRes int stringResId, @Nullable String string) {
        this.stringResId = stringResId;
        this.string = string;
    }

    @NonNull public String getString(@NonNull Context context) {
        if (stringResId != 0) {
            return context.getString(stringResId);
        } else {
            return string;
        }
    }
}
