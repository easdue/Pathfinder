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
