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

package nl.erikduisters.pathfinder.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.util.AttributeSet;

import nl.erikduisters.pathfinder.R;

public class VerticalProgressBar extends ProgressBarCompat {
    private static final int POSITION_TOP = 0;
    private static final int POSITION_MIDDLE = 1;
    private static final int POSITION_BOTTOM = 2;
    private String text;
    private int color;
    private float size;
    private Typeface typeface;
    private int textPosition;
    private Paint paint;
    private Rect bounds;

    private void init() {
        text = "";
        color = Color.BLACK;
        size = 15;
        typeface = Typeface.DEFAULT;
        textPosition = POSITION_MIDDLE;
        paint = new Paint();
        paint.setAntiAlias(true);
        bounds = new Rect();
    }

    public VerticalProgressBar(Context context) {
        this(context, null);
    }

    public VerticalProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.verticalProgressBarStyle);
    }

    public VerticalProgressBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
        setAttributes(attrs, defStyle);
    }

    private void setAttributes(AttributeSet attrs, int defStyle) {
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.VerticalProgressBar, defStyle, R.style.Widget_VerticalProgressBar);

            setText(a.getString(R.styleable.VerticalProgressBar_text));
            setTextColor(a.getColor(R.styleable.VerticalProgressBar_textColor, Color.BLACK));
            setTextSize(a.getDimension(R.styleable.VerticalProgressBar_textSize, 15));
            setStyle(a.getInt(R.styleable.VerticalProgressBar_textStyle, Typeface.NORMAL));
            setTextPosition(a.getInt(R.styleable.VerticalProgressBar_textPosition, POSITION_MIDDLE));

            a.recycle();
        }
    }

    @Override
    protected synchronized void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        paint.setColor(color);
        paint.setTextSize(size);
        paint.setTypeface(typeface);
        paint.getTextBounds(text, 0, text.length(), bounds);

        int x = getWidth() / 2 - bounds.centerX();
        int y;

        if (textPosition == POSITION_TOP) {
            y = bounds.height() + 10;
        } else if (textPosition == POSITION_MIDDLE) {
            y = getHeight() / 2 - bounds.centerY();
        } else {
            y = getHeight() - 10;
        }
        canvas.drawText(text, x, y, paint);
    }

    public synchronized void setText(String text) {
        this.text = text;
        if (this.text == null) {
            this.text = "";
        }
        invalidate();
    }

    public synchronized void setTextColor(int color) {
        this.color = color;
        invalidate();
    }

    public synchronized void setTextSize(float size) {
        this.size = size;
        invalidate();
    }

    public synchronized void setStyle(int style) {
        typeface = Typeface.defaultFromStyle(style);
        invalidate();
    }

    public synchronized void setTextPosition(int pos) {
        if (pos >= POSITION_TOP && pos <= POSITION_BOTTOM) {
            textPosition = pos;
        } else {
            textPosition = POSITION_MIDDLE;
        }
        invalidate();
    }
}