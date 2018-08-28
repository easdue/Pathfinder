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

package nl.erikduisters.pathfinder.ui.dialog.import_settings;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import nl.erikduisters.pathfinder.R;

/**
 * Created by Erik Duisters on 08-08-2018.
 */
public class ImportSettingsItemDecoration extends RecyclerView.ItemDecoration {
    final private Drawable divider;
    final private int dividerHeight;
    final private Rect bounds;

    public ImportSettingsItemDecoration(Context context) {
        TypedArray a = context.obtainStyledAttributes(new int[]{R.attr.dividerHorizontal});

        divider = a.getDrawable(0);

        a.recycle();

        if (divider != null) {
            dividerHeight = divider.getIntrinsicHeight();
        } else {
            dividerHeight = 0;
        }

        bounds = new Rect();
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        if (divider == null || dividerHeight == 0) {
            outRect.setEmpty();
            return;
        }

        final ImportSettingsAdapter adapter = (ImportSettingsAdapter) parent.getAdapter();
        final int itemCount = adapter.getItemCount();

        final int adapterPosition = parent.getChildAdapterPosition(view);
        int bottomPadding = 0;

        if (adapterPosition != RecyclerView.NO_POSITION) {
            final ImportSettingsAdapterData.Item item = adapter.getItemForAdapterPosition(adapterPosition);

            if (item != null && item.itemType() == ImportSettingsAdapterData.Item.ItemType.GROUP) {
                bottomPadding = dividerHeight;
            } else if (item != null) {
                if (adapterPosition < itemCount - 1) {
                    ImportSettingsAdapterData.Item nextItem = adapter.getItemForAdapterPosition(adapterPosition + 1);

                    if (nextItem != null && nextItem.itemType() == ImportSettingsAdapterData.Item.ItemType.GROUP) {
                        bottomPadding = dividerHeight;
                    }
                }
            }
        }

        //Vertical list so offset to the bottom (i.e. draw separator beneath to list item
        outRect.set(0, 0, 0, bottomPadding);
    }

    @Override
    public void onDraw(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
        if (divider == null || dividerHeight == 0) {
            return;
        }

        canvas.save();

        int left = 0;
        int right = parent.getWidth();

        if (parent.getClipToPadding()) {
            left = parent.getPaddingLeft();
            right -= parent.getPaddingRight();
            canvas.clipRect(left, parent.getPaddingTop(), right, parent.getHeight() - parent.getPaddingBottom());
        }

        final ImportSettingsAdapter adapter = (ImportSettingsAdapter) parent.getAdapter();
        final int itemCount = adapter.getItemCount();
        final int childCount = parent.getChildCount();

        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);

            final int adapterPosition = parent.getChildAdapterPosition(child);

            if (adapterPosition == RecyclerView.NO_POSITION) {
                continue;
            }

            final ImportSettingsAdapterData.Item item = adapter.getItemForAdapterPosition(adapterPosition);

            if (item != null && item.itemType() == ImportSettingsAdapterData.Item.ItemType.GROUP) {
                drawBelow(child, canvas, parent, left, right);
            } else if (item != null) {
                if (adapterPosition < itemCount - 1) {
                    ImportSettingsAdapterData.Item nextItem = adapter.getItemForAdapterPosition(adapterPosition + 1);

                    if (nextItem != null && nextItem.itemType() == ImportSettingsAdapterData.Item.ItemType.GROUP) {
                        drawBelow(child, canvas, parent, left, right);
                    }
                }
            }
        }

        canvas.restore();
    }

    private void drawBelow(View child, Canvas canvas, RecyclerView parent, int left, int right) {
        parent.getDecoratedBoundsWithMargins(child, bounds);

        final int bottom = bounds.bottom + Math.round(child.getTranslationY());
        final int top = bottom - dividerHeight;

        divider.setBounds(left, top, right, bottom);
        divider.draw(canvas);
    }
}
