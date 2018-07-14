package nl.erikduisters.pathfinder.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.erikduisters.pathfinder.R;
import nl.erikduisters.pathfinder.async.UseCaseJob;
import nl.erikduisters.pathfinder.data.local.SvgRenderer;
import nl.erikduisters.pathfinder.util.ViewUtil;

/*
 * Created by Erik Duisters on 13-07-2018.
 */
public class SvgView extends View {
    public interface SVGViewLoadedListener {
        void onSvgRendered(SvgView v);
        void onSvgRenderingFailed(Throwable e);
    }

    //With dagger android it's impossible to inject custom views at the moment
    private static SvgRenderer svgRenderer;

    private int svgResourceId = 0;
    private int maxWidth = Integer.MAX_VALUE;
    private int maxHeight = Integer.MAX_VALUE;
    private boolean adjustViewBounds = false;
    Paint bitmapPaint;
    private int width = 0;
    private int height = 0;
    private int minWidth = 1;
    private int minHeight = 1;
    private int density;
    private SVGViewLoadedListener listener = null;
    private UseCaseJob<SvgView, ?> renderJob;
    private boolean rendered = false;
    private List<String> layerList;
    private Map<String, Bitmap> layerBitmaps;

    public SvgView(Context context) {
        this(context, null);
    }

    public SvgView(Context context, AttributeSet attrs) {
        this(context, attrs, 0 /*R.attr.svgViewStyle*/);
    }

    public SvgView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SvgView, defStyle, 0 /*R.style.Widget_SvgViewStyle*/);

            svgResourceId = a.getResourceId(R.styleable.SvgView_android_src, 0);
            minWidth = a.getDimensionPixelSize(R.styleable.SvgView_android_minWidth, 1);
            minHeight = a.getDimensionPixelSize(R.styleable.SvgView_android_minHeight, 1);
            maxWidth = a.getDimensionPixelSize(R.styleable.SvgView_android_maxWidth, Integer.MAX_VALUE);
            maxHeight = a.getDimensionPixelSize(R.styleable.SvgView_android_maxHeight, Integer.MAX_VALUE);
            adjustViewBounds = a.getBoolean(R.styleable.SvgView_android_adjustViewBounds, false);

            a.recycle();
        }

        density = getResources().getDisplayMetrics().densityDpi;
        bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG | Paint.FILTER_BITMAP_FLAG);

        layerList = new ArrayList<>();
        layerList.add(null);

        layerBitmaps = new HashMap<>();

        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    public static void init(SvgRenderer svgRenderer) {
        SvgView.svgRenderer = svgRenderer;
    }

    public void setRenderJob(UseCaseJob<SvgView, ?> job) {
        renderJob = job;
    }

    public UseCaseJob<SvgView, ?> getRenderJob() {
        return renderJob;
    }

    void render() {
        if (width == 0 || height == 0 || svgResourceId == 0 || svgRenderer == null) {
            return;
        }

        rendered = false;
        svgRenderer.render(this);
    }

    public int getSvgResourceId() {
        return svgResourceId;
    }

    public void setSvgResourceId(int svgResId) {
        if (svgResId != svgResourceId) {
            svgResourceId = svgResId;

            if (svgRenderer != null) {
                render();
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int w;
        int h;

        float desiredAspect = 0.0f;

        boolean canResizeWidth = false;
        boolean canResizeHeight = false;

        final int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);

        Bitmap bitmap = layerBitmaps.get(layerList.get(0));

        if (bitmap == null) {
            /* Only available for API >= 16
            w = getMinimumWidth();
            h = getMinimumHeight();
            */
            w = minWidth;
            h = minHeight;
        } else {
            w = bitmap.getWidth();
            h = bitmap.getHeight();

        }

        if (adjustViewBounds) {
            canResizeWidth = widthSpecMode != MeasureSpec.EXACTLY;
            canResizeHeight = heightSpecMode != MeasureSpec.EXACTLY;

            desiredAspect = (float) w / (float) h;
        }

        int pLeft = getPaddingLeft();
        int pRight = getPaddingRight();
        int pTop = getPaddingTop();
        int pBottom = getPaddingBottom();

        int width;
        int height;

        if (canResizeWidth || canResizeHeight) {
            width = ViewUtil.resolveSizeAndState(w + pLeft + pRight, maxWidth, widthMeasureSpec);
            height = ViewUtil.resolveSizeAndState(h + pTop + pBottom, maxHeight, heightMeasureSpec);

            if (desiredAspect != 0.0f) {
                // See what our actual aspect ratio is
                float actualAspect = (float) (width - pLeft - pRight) /
                        (height - pTop - pBottom);

                if (Math.abs(actualAspect - desiredAspect) > 0.0000001) {
                    boolean done = false;

                    // Try adjusting width to be proportional to height
                    if (canResizeWidth) {
                        int newWidth = (int) (desiredAspect * (height - pTop - pBottom)) +
                                pLeft + pRight;
                        if (newWidth <= width) {
                            width = newWidth;
                            done = true;
                        }
                    }

                    // Try adjusting height to be proportional to width
                    if (!done && canResizeHeight) {
                        int newHeight = (int) ((width - pLeft - pRight) / desiredAspect) +
                                pTop + pBottom;
                        if (newHeight <= height) {
                            height = newHeight;
                        }
                    }
                }
            }
        } else {
            /* We are either don't want to preserve the drawables aspect ratio,
             * or we are not allowed to change view dimensions. Just measure in
             * the normal way.
             */
            w += pLeft + pRight;
            h += pTop + pBottom;

            w = Math.max(w, getSuggestedMinimumWidth());
            h = Math.max(h, getSuggestedMinimumHeight());

            width = ViewUtil.resolveSizeAndState(w, widthMeasureSpec);
            height = ViewUtil.resolveSizeAndState(h, heightMeasureSpec);
        }

        setMeasuredDimension(width, height);

        this.width = width - pLeft - pRight;
        this.height = height - pTop - pBottom;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (this.isInEditMode()) {
            return;
        }

        for (String layer : layerList) {
            Bitmap bitmap = layerBitmaps.get(layer);

            if (bitmap != null) {
                bitmap.recycle();
            }
        }

        layerBitmaps.clear();

        render();
    }

    public void setBitmapLayers(Map<String, Bitmap> bitmapMap) {
        layerBitmaps = bitmapMap;
    }

    public void onRenderComplete() {
        invalidate();

        rendered = true;

        if (listener != null) {
            listener.onSvgRendered(this);
        }
    }

    public void onRenderFailed(Throwable e) {
        if (listener != null) {
            listener.onSvgRenderingFailed(e);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (!layerBitmaps.isEmpty()) {
            canvas.translate(getPaddingLeft(), getPaddingTop());

            for (String layer : layerList) {
                canvas.drawBitmap(layerBitmaps.get(layer), 0, 0, bitmapPaint);
            }
        }
    }

    public Bitmap getBitmap() {
        return layerBitmaps.get(null);
    }

    public Bitmap getBitmap(String layer) {
        return layerBitmaps.get(layer);
    }

    public void addLayerToRender(@NonNull String layer) {
        if (layerList.contains(null)) {
            layerList.clear();
        }

        layerList.add(layer);
    }

    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;

        onSizeChanged(width, height, 0, 0);
    }

    public int getSvgWidth() {
        return width;
    }

    public int getSvgHeight() {
        return height;
    }

    public int getSvgDensity() {
        return density;
    }

    public List<String> getLayerList() {
        return layerList;
    }

    public void setListener(SVGViewLoadedListener listener) {
        this.listener = listener;
    }

    public boolean isRendered() {
        return rendered;
    }

    public void setTint(@ColorInt int tint, PorterDuff.Mode tintMode) {
        PorterDuffColorFilter filter = new PorterDuffColorFilter(tint, tintMode);
        bitmapPaint.setColorFilter(filter);
        invalidate();
    }

    public void setColorFilter(ColorFilter colorFilter) {
        bitmapPaint.setColorFilter(colorFilter);
        invalidate();
    }
}