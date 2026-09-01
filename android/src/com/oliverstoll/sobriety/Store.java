package com.oliverstoll.sobriety;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/** Trackers live in a single SharedPreferences entry as a JSON array. */
public final class Store {

    private static final String PREFS = "sobriety";
    private static final String KEY = "trackers";

    private Store() {}

    static SharedPreferences prefs(Context ctx) {
        return ctx.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static List<Tracker> load(Context ctx) {
        List<Tracker> out = new ArrayList<Tracker>();
        String raw = prefs(ctx).getString(KEY, null);
        if (raw == null) return out;
        try {
            JSONArray arr = new JSONArray(raw);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.optJSONObject(i);
                if (o != null) out.add(Tracker.fromJson(o));
            }
        } catch (Exception e) {
            // Corrupt payload: start over rather than crash on launch.
        }
        return out;
    }

    public static void save(Context ctx, List<Tracker> trackers) {
        JSONArray arr = new JSONArray();
        try {
            for (Tracker t : trackers) arr.put(t.toJson());
        } catch (Exception e) {
            return;
        }
        prefs(ctx).edit().putString(KEY, arr.toString()).apply();
        notifyWidgets(ctx);
    }

    /**
     * Redraw every placed widget after the data or the appearance changed.
     *
     * <p>Repaints them here rather than broadcasting ACTION_APPWIDGET_UPDATE to
     * our own receiver: the row contents come from the RemoteViewsFactory via
     * notifyAppWidgetViewDataChanged, but the widget's frame — background
     * colour, outer padding — is only set in the provider's render pass, so
     * relying on a broadcast to reach it left the background stale.
     */
    public static void notifyWidgets(Context ctx) {
        Context app = ctx.getApplicationContext();
        AppWidgetManager mgr = AppWidgetManager.getInstance(app);
        ComponentName provider = new ComponentName(app, SobrietyWidget.class);
        int[] ids = mgr.getAppWidgetIds(provider);
        if (ids == null || ids.length == 0) return;
        SobrietyWidget.renderAll(app, mgr, ids);
        mgr.notifyAppWidgetViewDataChanged(ids, R.id.widget_list);
    }
}
