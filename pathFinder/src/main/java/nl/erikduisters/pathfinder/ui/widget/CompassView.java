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
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrixColorFilter;
import android.util.AttributeSet;

import java.util.Map;

import nl.erikduisters.pathfinder.R;
import nl.erikduisters.pathfinder.util.IntegerDegrees;

/**
 * Created by Erik Duisters on 14-07-2018.
 */

public class CompassView extends SvgView {
    private Bitmap compassCaseBitmap = null;
    private Bitmap compassFaceBitmap = null;
    private Bitmap compassNeedleBitmap = null;
    private Bitmap compassBearingBitmap = null;
    private float compassCenterX;
    private float compassCenterY;
    private int bearing = 0;
    private int heading = -1;
    private final RotationCalculator headingCalculator;
    private final RotationCalculator bearingCalculator;
    private final ColorMatrixColorFilter colorMatrixColorFilter;

    /**
     * Calculates rotation using angular motion equation of magnetic dipole in magnetic field
     * based on https://github.com/Salauyou/CompassView
     *
     * @author Erik Duisters
     */
    public static class RotationCalculator {
        private static final float TIME_DELTA_THRESHOLD = 0.25f;    // maximum time difference between iterations, s
        private static final float ANGLE_DELTA_THRESHOLD = 0.1f;    // minimum rotation change to be redrawn, deg

        private static final float INERTIA_MOMENT_DEFAULT = 0.1f;    // default physical properties
        private static final float ALPHA_DEFAULT = 10;
        private static final float MB_DEFAULT = 1000;

        private long time1, time2;                  // timestamps of previous iterations--used in numerical integration
        private float angle0, angle1, angle2;       // angles of previous iterations
        private float angleLastDrawn;               // last drawn anglular position
        private boolean animationOn;                // if animation should be performed

        private float inertiaMoment;                // moment of inertia
        private float alpha;                        // damping coefficient
        private float mB;                          // magnetic field coefficient

        /* The parameters below are used in onDraw(). Don't allocate in onDraw() */
        private float deltaT1;
        private float deltaT2;
        private float koefI;
        private float koefAlpha;
        private float koefK;
        private float angleNew;
        private float normalizedAngle;

        public RotationCalculator() {
            time1 = time2 = 0L;
            angle0 = angle1 = angle2 = 360f;
            angleLastDrawn = 360f;
            animationOn = false;
            inertiaMoment = INERTIA_MOMENT_DEFAULT;
            alpha = ALPHA_DEFAULT;
            mB = MB_DEFAULT;
        }

        /**
         * Set the current target rotation
         *
         * @param newAngle The target rotation angle
         * @param animate  if true animate towards target angle.
         */
        public void setTargetRotation(final float newAngle, final boolean animate) {
            if (animate) {
                if (Math.abs(angle0 - newAngle) > ANGLE_DELTA_THRESHOLD) {
                    angle0 = newAngle;
                    animationOn = true;
                }
            } else {
                angle0 = newAngle;
                angle1 = newAngle;
                angle2 = newAngle;
                angleLastDrawn = newAngle;
                animationOn = false;
            }
        }

        /**
         * Get the current rotation angle
         *
         * @return The current rotation angle
         */
        public float getCurrentRotation() {
            return angle1;
        }

        public boolean isAnimating() {
            return animationOn;
        }

        /**
         * Use this to set physical properties.
         * Negative values will be replaced by default values
         *
         * @param inertiaMoment Moment of inertia (default 0.1)
         * @param alpha         Damping coefficient (default 10)
         * @param mB            Magnetic field coefficient (default 1000)
         */
        public void setPhysicalProperties(float inertiaMoment, float alpha, float mB) {
            this.inertiaMoment = inertiaMoment >= 0 ? inertiaMoment : INERTIA_MOMENT_DEFAULT;
            this.alpha = alpha >= 0 ? alpha : ALPHA_DEFAULT;
            this.mB = mB >= 0 ? mB : MB_DEFAULT;
        }


        /**
         * Recalculate angles using equation of dipole circular motion
         *
         * @param timeNew Current timestamp in milliseconds
         */
        void recalculateRotation(final long timeNew) {

            // recalculate angle using simple numerical integration of motion equation
            deltaT1 = (timeNew - time1) / 1000f;
            if (deltaT1 > TIME_DELTA_THRESHOLD) {
                deltaT1 = TIME_DELTA_THRESHOLD;
                time1 = timeNew + Math.round(TIME_DELTA_THRESHOLD * 1000);
            }
            deltaT2 = (time1 - time2) / 1000f;    //Nanoseconds to seconds
            if (deltaT2 > TIME_DELTA_THRESHOLD) {
                deltaT2 = TIME_DELTA_THRESHOLD;
            }

            // circular acceleration coefficient
            koefI = inertiaMoment / deltaT1 / deltaT2;

            // circular velocity coefficient
            koefAlpha = alpha / deltaT1;

            // angular momentum coefficient
            koefK = mB * (float) (Math.sin(Math.toRadians(angle0)) * Math.cos(Math.toRadians(angle1)) -
                    (Math.sin(Math.toRadians(angle1)) * Math.cos(Math.toRadians(angle0))));

            angleNew = (koefI * (angle1 * 2f - angle2) + koefAlpha * angle1 + koefK) / (koefI + koefAlpha);

            // reassign previous iteration variables
            angle2 = angle1;
            angle1 = angleNew;
            time2 = time1;
            time1 = timeNew;

            //if angles changed more then threshold, draw at new angle
            if (Math.abs(angleLastDrawn - angle1) >= ANGLE_DELTA_THRESHOLD) {
                angleLastDrawn = angle1;
            } else {
                normalizedAngle = angleNew % 360;
                if (normalizedAngle < 0) {
                    normalizedAngle += 360;
                }

                if (Math.abs(angle0 - normalizedAngle) < ANGLE_DELTA_THRESHOLD) {
                    animationOn = false;
                }
            }
        }
    }

    public CompassView(Context context) {
        this(context, null);
    }

    public CompassView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.compassStyle);
    }

    public CompassView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        //setAttributes(attrs, defStyle);

        float[] matrix = {
                0.3f, 0.49f, 0.3f, 0, 0,
                0.3f, 0.49f, 0.3f, 0, 0,
                0.3f, 0.49f, 0.3f, 0, 0,
                0, 0, 0, 0.4f, 0
        };

        colorMatrixColorFilter = new ColorMatrixColorFilter(matrix);

        headingCalculator = new RotationCalculator();
        bearingCalculator = new RotationCalculator();

        addLayerToRender("layer1");
        addLayerToRender("layer2");
        addLayerToRender("layer3");
        addLayerToRender("layer4");
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (compassCaseBitmap != null) {
            if (headingCalculator.isAnimating()) {
                headingCalculator.recalculateRotation(System.nanoTime() / 1000000L);
            }

            if (bearingCalculator.isAnimating()) {
                bearingCalculator.recalculateRotation(System.nanoTime() / 1000000L);
            }

            canvas.translate(getPaddingLeft(), getPaddingTop());
            canvas.drawBitmap(compassCaseBitmap, 0, 0, bitmapPaint);
            canvas.save();
            canvas.rotate(headingCalculator.getCurrentRotation(), compassCenterX, compassCenterY);
            canvas.drawBitmap(compassFaceBitmap, 0, 0, bitmapPaint);
            canvas.rotate(bearingCalculator.getCurrentRotation(), compassCenterX, compassCenterY);
            canvas.drawBitmap(compassNeedleBitmap, 0, 0, bitmapPaint);
            canvas.restore();

            if (heading == -1) {
                bitmapPaint.setColorFilter(colorMatrixColorFilter);
            }

            if (bearing != -1) {
                canvas.drawBitmap(compassBearingBitmap, 0, 0, bitmapPaint);
            }

            bitmapPaint.setColorFilter(null);

            if (headingCalculator.isAnimating() || bearingCalculator.isAnimating()) {
                invalidate();
            }
        }
    }

    @Override
    public void setBitmapLayers(Map<String, Bitmap> bitmapMap) {
        compassCaseBitmap = bitmapMap.get("layer1");
        compassFaceBitmap = bitmapMap.get("layer2");
        compassNeedleBitmap = bitmapMap.get("layer3");
        compassBearingBitmap = bitmapMap.get("layer4");

        compassCenterX = compassCaseBitmap.getWidth() / 2;
        compassCenterY = compassCaseBitmap.getHeight() / 2;
    }

    public void setBearing(IntegerDegrees bearing) {
        int newBearing = bearing.get();

        if (newBearing == 0) {
            newBearing = 360;
        }

        if (bearing.isUnknown()) {
            this.bearing = -1;

            bearingCalculator.setTargetRotation(360, true);

            invalidate();
        } else if (this.bearing != newBearing) {
            this.bearing = newBearing;

            bearingCalculator.setTargetRotation(this.bearing, true);

            invalidate();
        }
    }

    public void setHeading(IntegerDegrees heading) {
        if (heading.isUnknown()) {
            if (this.heading != -1) {
                this.heading = -1;
                invalidate();
            }
        } else if (this.heading != 360 - heading.get()) {
            this.heading = 360 - heading.get();

            headingCalculator.setTargetRotation(this.heading, true);

            invalidate();
        }
    }
}