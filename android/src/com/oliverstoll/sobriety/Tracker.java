package com.oliverstoll.sobriety;

import org.json.JSONException;
import org.json.JSONObject;

/** One thing being tracked: an icon, a name, and the day sobriety started. */
public class Tracker {
    public String id;
    public String icon;
    public String name;
    /** Exactly when sobriety started, in millis. */
    public long startMillis;

    public Tracker(String id, String icon, String name, long startMillis) {
        this.id = id;
        this.icon = icon;
        this.name = name;
        this.startMillis = startMillis;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("id", id);
        o.put("icon", icon);
        o.put("name", name);
        o.put("start", startMillis);
        return o;
    }

    public static Tracker fromJson(JSONObject o) {
        return new Tracker(
                o.optString("id", String.valueOf(System.nanoTime())),
                o.optString("icon", "✨"),
                o.optString("name", ""),
                o.optLong("start", System.currentTimeMillis()));
    }

    /** Milliseconds of sobriety so far; negative if the start is in the future. */
    public long elapsed() {
        return System.currentTimeMillis() - startMillis;
    }
}
