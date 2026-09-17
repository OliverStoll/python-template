package com.oliverstoll.sobriety;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.widget.RemoteViews;

public class SobrietyWidget extends AppWidgetProvider {

    /** Our own wake-up, fired when a displayed number is about to go stale. */
    static final String ACTION_TICK = "com.oliverstoll.sobriety.TICK";

    /** Never sleep longer than this, so a missed alarm cannot strand the widget. */
    private static final long MAX_SLEEP = Format.HOUR;
    /** Nor shorter than this, so a counter started seconds ago cannot spin. */
    private static final long MIN_SLEEP = 15000L;

    @Override
    public void onUpdate(Context ctx, AppWidgetManager mgr, int[] appWidgetIds) {
        renderAll(ctx, mgr, appWidgetIds);
    }

    /**
     * Redraws widgets directly, without going through a broadcast.
     *
     * <p>{@code updateAppWidget} is callable from any process that owns the
     * widget, so the app can repaint its own widgets synchronously. Asking the
     * system to deliver ACTION_APPWIDGET_UPDATE back to this receiver instead
     * is a longer path with more ways to be dropped.
     */
    static void renderAll(Context ctx, AppWidgetManager mgr, int[] appWidgetIds) {
        if (appWidgetIds == null) return;
        for (int id : appWidgetIds) render(ctx, mgr, id);
        scheduleNextTick(ctx, appWidgetIds.length > 0);
    }

    @Override
    public void onDisabled(Context ctx) {
        super.onDisabled(ctx);
        scheduleNextTick(ctx, false);
    }

    /**
     * Wakes the widget exactly when its soonest counter changes value.
     *
     * <p>updatePeriodMillis bottoms out at 30 minutes, which is useless for a
     * counter reading in minutes. Instead we ask each counter how long until
     * its number moves and set one alarm for the earliest — a minute apart in
     * the first hour, an hour apart in the first day, then daily.
     */
    private static void scheduleNextTick(Context ctx, boolean wanted) {
        Context app = ctx.getApplicationContext();
        AlarmManager alarms = (AlarmManager) app.getSystemService(Context.ALARM_SERVICE);
        if (alarms == null) return;

        Intent tick = new Intent(app, SobrietyWidget.class).setAction(ACTION_TICK);
        PendingIntent pending = PendingIntent.getBroadcast(app, 0, tick,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        alarms.cancel(pending);
        if (!wanted) return;

        int mode = Settings.unitMode(app);
        long soonest = MAX_SLEEP;
        for (Tracker t : Store.load(app)) {
            long until = Format.millisUntilChange(t.elapsed(), mode);
            if (until < soonest) soonest = until;
        }
        if (soonest < MIN_SLEEP) soonest = MIN_SLEEP;
        if (soonest > MAX_SLEEP) soonest = MAX_SLEEP;

        // Inexact on purpose: this is cosmetic, and exact alarms need a
        // permission prompt on Android 12+ that a widget does not deserve.
        alarms.set(AlarmManager.RTC, System.currentTimeMillis() + soonest, pending);
    }

    @Override
    public void onReceive(Context ctx, Intent intent) {
        super.onReceive(ctx, intent);
        String action = intent.getAction();
        if (ACTION_TICK.equals(action)
                || Intent.ACTION_DATE_CHANGED.equals(action)
                || Intent.ACTION_TIME_CHANGED.equals(action)
                || Intent.ACTION_TIMEZONE_CHANGED.equals(action)) {
            Store.notifyWidgets(ctx);
        }
    }

    private static void render(Context ctx, AppWidgetManager mgr, int widgetId) {
        RemoteViews views = new RemoteViews(ctx.getPackageName(), R.layout.widget);

        Settings.Values look = Settings.load(ctx);
        int outerH = Settings.dpToPx(ctx, look.outerPaddingHorizontalDp());
        int outerV = Settings.dpToPx(ctx, look.outerPaddingVerticalDp());
        views.setViewPadding(R.id.widget_content, outerH, outerV, outerH, outerV);

        // Tint the background image rather than the View's background, so the
        // rounded corners survive; alpha rides separately from the RGB tint.
        views.setInt(R.id.widget_bg, "setColorFilter", Settings.opaque(look.bgColor));
        views.setInt(R.id.widget_bg, "setImageAlpha", Color.alpha(look.bgColor));

        views.setTextColor(R.id.widget_empty, look.unitColor());

        Intent data = new Intent(ctx, WidgetService.class);
        data.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        // A unique data URI keeps each widget instance's adapter distinct.
        data.setData(Uri.parse(data.toUri(Intent.URI_INTENT_SCHEME)));
        views.setRemoteAdapter(R.id.widget_list, data);
        // The AdapterView swaps these two itself; don't set visibility by hand.
        views.setEmptyView(R.id.widget_list, R.id.widget_empty);

        // One template for the whole list; the fill-in intents decide whether a
        // tap opens the app or resets that counter.
        Intent tap = new Intent(ctx, WidgetTapActivity.class);
        PendingIntent template = PendingIntent.getActivity(ctx, 0, tap,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        views.setPendingIntentTemplate(R.id.widget_list, template);
        views.setOnClickPendingIntent(R.id.widget_empty,
                PendingIntent.getActivity(ctx, 1, new Intent(ctx, MainActivity.class),
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE));

        mgr.updateAppWidget(widgetId, views);
        mgr.notifyAppWidgetViewDataChanged(widgetId, R.id.widget_list);
    }
}
