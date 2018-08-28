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

package nl.erikduisters.pathfinder.ui.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class MyViewPager extends ViewPager {
    private boolean pagingEnabled;

    public MyViewPager(Context context) {
        this(context, null);
    }

    public MyViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);

        pagingEnabled = true;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return pagingEnabled && super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return pagingEnabled && super.onInterceptTouchEvent(event);
    }

    public void setPagingEnabled(boolean enabled) {
        pagingEnabled = enabled;
    }
}
