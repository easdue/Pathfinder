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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.List;

import nl.erikduisters.pathfinder.data.local.GpsManager;

/*
 * Based on https://github.com/barbeau/gpstest
 */

public class GpsSkyView extends View {
    private Paint horizonStrokePaint;
    private Paint gridStrokePaint;
    private Paint satelliteFillPaint;
    private Paint satelliteStrokePaint;
    private List<GpsManager.SatelliteInfo> satInfoList;
    private float snrThresholds[];
    private int snrColors[];
    private float radius;
    private float xCorrection;
    private float yCorrection;
    private RectF bounds;

    private static final int SAT_RADIUS = 10;

    public GpsSkyView(Context context) {
        super(context);
        init();
    }

    public GpsSkyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GpsSkyView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        horizonStrokePaint = new Paint();
        horizonStrokePaint.setColor(Color.BLACK);
        horizonStrokePaint.setStyle(Paint.Style.STROKE);
        horizonStrokePaint.setStrokeWidth(2.0f);
        horizonStrokePaint.setAntiAlias(true);

        gridStrokePaint = new Paint();
        gridStrokePaint.setColor(Color.GRAY);
        gridStrokePaint.setStyle(Paint.Style.STROKE);
        gridStrokePaint.setStrokeWidth(1.5f);
        gridStrokePaint.setAntiAlias(true);

        satelliteFillPaint = new Paint();
        satelliteFillPaint.setColor(Color.YELLOW);
        satelliteFillPaint.setStyle(Paint.Style.FILL);
        satelliteFillPaint.setAntiAlias(true);

        satelliteStrokePaint = new Paint();
        satelliteStrokePaint.setColor(Color.BLACK);
        satelliteStrokePaint.setStyle(Paint.Style.STROKE);
        satelliteStrokePaint.setStrokeWidth(2.0f);
        satelliteStrokePaint.setAntiAlias(true);

        snrThresholds = new float[]{0.0f, 10.0f, 20.0f, 30.0f};
        snrColors = new int[]{Color.WHITE, Color.RED, Color.YELLOW, Color.GREEN};

        setFocusable(true);
    }

    public void setSatellites(List<GpsManager.SatelliteInfo> satInfoList) {
        this.satInfoList = satInfoList;

        invalidate();
    }

    private void drawHorizon(Canvas c) {
        c.drawLine(bounds.centerX(), bounds.centerY() - radius, bounds.centerX(), bounds.centerY() + radius, gridStrokePaint);
        c.drawLine(bounds.centerX() - radius, bounds.centerY(), bounds.centerX() + radius, bounds.centerY(), gridStrokePaint);
        c.drawCircle(bounds.centerX(), bounds.centerY(), elevationToRadius(60.0f), gridStrokePaint);
        c.drawCircle(bounds.centerX(), bounds.centerY(), elevationToRadius(30.0f), gridStrokePaint);
        c.drawCircle(bounds.centerX(), bounds.centerY(), elevationToRadius(0.0f), gridStrokePaint);
        c.drawCircle(bounds.centerX(), bounds.centerY(), radius - horizonStrokePaint.getStrokeWidth(), horizonStrokePaint);
    }

    private void drawSatellite(Canvas c, GpsManager.SatelliteInfo sat) {
        double radius, angle;
        float x, y;
        Paint thisPaint;

        thisPaint = getSatellitePaint(satelliteFillPaint, sat.snr);

        radius = elevationToRadius(sat.elevation);
        angle = (float) Math.toRadians(sat.azimuth);

        x = (float) (xCorrection + this.radius + (radius * Math.sin(angle)));
        y = (float) (yCorrection + this.radius - (radius * Math.cos(angle)));

        c.drawCircle(x, y, SAT_RADIUS, thisPaint);
        c.drawCircle(x, y, SAT_RADIUS, satelliteStrokePaint);
    }

    private float elevationToRadius(float elev) {
        return (radius - SAT_RADIUS) * (1.0f - (elev / 90.0f));
    }

    private Paint getSatellitePaint(Paint base, float snr) {
        int numSteps;
        Paint newPaint;

        newPaint = new Paint(base);

        numSteps = snrThresholds.length;

        if (snr <= snrThresholds[0]) {
            newPaint.setColor(snrColors[0]);
            return newPaint;
        }

        if (snr >= snrThresholds[numSteps - 1]) {
            newPaint.setColor(snrColors[numSteps - 1]);
            return newPaint;
        }

        for (int i = 0; i < numSteps - 1; i++) {
            float threshold = snrThresholds[i];
            float nextThreshold = snrThresholds[i + 1];
            if (snr >= threshold && snr <= nextThreshold) {
                int c1, r1, g1, b1, c2, r2, g2, b2, c3, r3, g3, b3;
                float f;

                c1 = snrColors[i];
                r1 = Color.red(c1);
                g1 = Color.green(c1);
                b1 = Color.blue(c1);

                c2 = snrColors[i + 1];
                r2 = Color.red(c2);
                g2 = Color.green(c2);
                b2 = Color.blue(c2);

                f = (snr - threshold) / (nextThreshold - threshold);

                r3 = (int) (r2 * f + r1 * (1.0f - f));
                g3 = (int) (g2 * f + g1 * (1.0f - f));
                b3 = (int) (b2 * f + b1 * (1.0f - f));
                c3 = Color.rgb(r3, g3, b3);

                newPaint.setColor(c3);

                return newPaint;
            }
        }

        newPaint.setColor(Color.MAGENTA);

        return newPaint;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawHorizon(canvas);

        if (satInfoList != null) {
            for (GpsManager.SatelliteInfo sat : satInfoList) {
                if (sat.snr > 0.0f && (sat.elevation != 0.0f || sat.azimuth != 0.0f))
                    drawSatellite(canvas, sat);
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        float diameter;

        bounds = new RectF(getPaddingLeft(), getPaddingTop(), w - getPaddingRight(), h - getPaddingBottom());
        if (bounds.width() < bounds.height()) {
            diameter = bounds.width();
            yCorrection = (bounds.height() - diameter) / 2;
            xCorrection = 0;
        } else {
            diameter = bounds.height();
            yCorrection = 0;
            xCorrection = (bounds.width() - diameter) / 2;
        }
        radius = diameter / 2;
    }
}