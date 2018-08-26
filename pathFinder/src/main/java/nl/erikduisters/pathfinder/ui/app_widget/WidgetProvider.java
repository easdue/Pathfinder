package nl.erikduisters.pathfinder.ui.app_widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import nl.erikduisters.pathfinder.R;
import nl.erikduisters.pathfinder.ui.activity.main_activity.MainActivity;
import nl.erikduisters.pathfinder.util.Distance;

/**
 * Created by Erik Duisters on 25-08-2018.
 */
public class WidgetProvider extends AppWidgetProvider {
    static void updateWidgets(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId);
        }
    }

    private static void updateWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        WidgetInfo widgetInfo = WidgetService.widgetInfo;

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.app_widget);

        views.setTextViewText(R.id.availableTracks, String.valueOf(widgetInfo.numTracks));
        views.setTextViewText(R.id.activityTypes, String.valueOf(widgetInfo.numTrackActivityTypes));
        views.setTextViewText(R.id.averageLength, Distance.getDistance(context, widgetInfo.averageLength, 2));
        views.setTextViewText(R.id.totalLength, Distance.getDistance(context, widgetInfo.totalLength, 2));

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.clickFrame, pendingIntent);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
       WidgetService.startActionUpdateWidget(context);
    }
}
