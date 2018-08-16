package nl.erikduisters.pathfinder.glide;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

import java.nio.charset.Charset;
import java.security.MessageDigest;

import nl.erikduisters.pathfinder.R;

/**
 * Created by Erik Duisters on 14-08-2018.
 */
public class CropHeightGraphTransformation extends BitmapTransformation {
    private static final String ID = "nl.erikduisters.pathfinder.glide.CropHeightGraphTransformation";
    private static final byte[] ID_BYTES = ID.getBytes(Charset.forName("UTF-8"));

    private final int height;
    private final int width;

    public CropHeightGraphTransformation(Context context) {
        height = context.getResources().getDimensionPixelSize(R.dimen.height_chart_height);
        width = context.getResources().getDimensionPixelSize(R.dimen.height_chart_width);
    }

    /*
          mdpi 160x80  (Don't use this, downscale from hdpi 140x64
          hdpi 240x120 x: 26-234 y: 0 - 98   208x98   210x98    ^xstart=26 ^xend=6 ^ystart=0 ^yend=22
         xhdpi 320x160 x: 26-314 y: 0 - 138  288x138  280x128
        xxhdpi 480x240 x: 26-474 y: 0 - 218  448x218  420x204
    */
    @Override
    protected Bitmap transform(@NonNull BitmapPool pool, @NonNull Bitmap toTransform, int outWidth, int outHeight) {
        Bitmap croppedBitmap = Bitmap.createBitmap(toTransform, 26, 0, width - 32, height - 22);

        return croppedBitmap;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof CropHeightGraphTransformation;
    }

    @Override
    public int hashCode() {
        return ID.hashCode();
    }

    @Override
    public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {
        messageDigest.update(ID_BYTES);
    }
}
