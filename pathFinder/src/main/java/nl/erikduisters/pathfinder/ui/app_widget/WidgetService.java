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

package nl.erikduisters.pathfinder.ui.app_widget;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import nl.erikduisters.pathfinder.data.local.database.PathfinderDatabase;
import timber.log.Timber;

/**
 * Created by Erik Duisters on 26-08-2018.
 */
public class WidgetService extends IntentService {
    private static final String ACTION_UPDATE_WIDGET = "com.erikduisters.pathfinder.action.update_widget";
    public static WidgetInfo widgetInfo;

    @Inject PathfinderDatabase database;

    public WidgetService() {
        super(WidgetService.class.getSimpleName());

        Timber.e("new WidgetService created");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        AndroidInjection.inject(this);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_UPDATE_WIDGET.equals(action)) {
                widgetInfo = database.trackDao().getWidgetInfo();

                updateWidget();
            }
        }
    }

    private void updateWidget() {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getBaseContext());

        int[] widgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(getBaseContext(), WidgetProvider.class));

        WidgetProvider.updateWidgets(getBaseContext(), appWidgetManager, widgetIds);
    }

    public static void startActionUpdateWidget(Context context) {
        Intent intent = new Intent(context, WidgetService.class);
        intent.setAction(ACTION_UPDATE_WIDGET);

        context.startService(intent);
    }
}
