package com.oliverstoll.sobriety;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

public class SobrietyWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context ctx, AppWidgetManager mgr, int[] appWidgetIds) {
        for (int id : appWidgetIds) render(ctx, mgr, id);
    }

    @Override
    public void onReceive(Context ctx, Intent intent) {
        super.onReceive(ctx, intent);
        String action = intent.getAction();
        if (Intent.ACTION_DATE_CHANGED.equals(action)
                || Intent.ACTION_TIME_CHANGED.equals(action)
                || Intent.ACTION_TIMEZONE_CHANGED.equals(action)) {
            // A new day means every counter moved on.
            Store.notifyWidgets(ctx);
        }
    }

    private void render(Context ctx, AppWidgetManager mgr, int widgetId) {
        RemoteViews views = new RemoteViews(ctx.getPackageName(), R.layout.widget);

        Intent data = new Intent(ctx, WidgetService.class);
        data.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        // A unique data URI keeps each widget instance's adapter distinct.
        data.setData(Uri.parse(data.toUri(Intent.URI_INTENT_SCHEME)));
        views.setRemoteAdapter(R.id.widget_list, data);
        // The AdapterView swaps these two itself; don't set visibility by hand.
        views.setEmptyView(R.id.widget_list, R.id.widget_empty);

        // Tapping any row opens the app.
        Intent open = new Intent(ctx, MainActivity.class);
        PendingIntent template = PendingIntent.getActivity(ctx, 0, open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setPendingIntentTemplate(R.id.widget_list, template);
        views.setOnClickPendingIntent(R.id.widget_empty, template);

        mgr.updateAppWidget(widgetId, views);
        mgr.notifyAppWidgetViewDataChanged(widgetId, R.id.widget_list);
    }
}
