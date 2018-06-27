package nl.erikduisters.pathfinder.util;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.content.res.AppCompatResources;

import java.io.File;

/**
 * Created by Erik Duisters on 27-06-2018.
 */
public class DrawableType {
    private @DrawableRes int drawableResId;
    private @Nullable File file;

    public DrawableType(@DrawableRes int drawableResId) {
        this(drawableResId, null);
    }

    public DrawableType(@NonNull File file) {
        this(0, file);
    }

    private DrawableType(@DrawableRes int drawableResId, @Nullable File file) {
        this.drawableResId = drawableResId;
        this.file = file;
    }

    @NonNull public Drawable getDrawable(@NonNull Context context) {
        if (drawableResId != 0) {
            Drawable drawable = AppCompatResources.getDrawable(context, drawableResId);

            if (drawable == null) {
                throw new RuntimeException("Drawable for resourceId: " + drawableResId + " cannot be created");
            }

            return drawable;
        } else {
            if (file == null) {
                throw new IllegalStateException("Using reflection are we?");
            }

            return new BitmapDrawable(context.getResources(), file.getAbsolutePath());
        }
    }
}
