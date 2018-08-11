package nl.erikduisters.pathfinder.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ProgressBar;

import nl.erikduisters.pathfinder.R;

/**
 * Created by Erik Duisters on 23-03-2015.
 * <p/>
 * Apply's a color filter to the progressDrawable and IntermediateDrawable so the
 * progress indicator looks like on lollipop
 */

public class ProgressBarCompat extends ProgressBar {
    private int accentColor;

    public ProgressBarCompat(Context context) {
        super(context);
    }

    public ProgressBarCompat(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ProgressBarCompat(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void getColorAccent() {
        if (accentColor == 0 && Build.VERSION.SDK_INT >= 15 && Build.VERSION.SDK_INT < 21) {
            TypedArray a;
            a = getContext().getTheme().obtainStyledAttributes(new int[]{R.attr.colorAccent});

            accentColor = a.getColor(0, 0);

            a.recycle();
        }

    }

    private void applyColorFilter(Drawable d) {
        if (Build.VERSION.SDK_INT >= 15 && Build.VERSION.SDK_INT < 21) {
            if (d instanceof LayerDrawable) {
                LayerDrawable ld = (LayerDrawable) d;

                Drawable bg = ld.findDrawableByLayerId(android.R.id.progress);

                if (bg != null) {
                    bg.setColorFilter(accentColor, PorterDuff.Mode.SRC_IN);
                    return;
                }

                d.setColorFilter(accentColor, PorterDuff.Mode.SRC_IN);
            } else {
                d.setColorFilter(accentColor, PorterDuff.Mode.SRC_IN);
            }
        }
    }

    @Override
    public void setProgressDrawable(Drawable d) {
        super.setProgressDrawable(d);

        getColorAccent();
        applyColorFilter(d);
    }

    @Override
    public void setIndeterminateDrawable(Drawable d) {
        super.setIndeterminateDrawable(d);

        getColorAccent();
        applyColorFilter(d);
    }
}