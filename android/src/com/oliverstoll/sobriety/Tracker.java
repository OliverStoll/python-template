package com.oliverstoll.sobriety;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** One thing being tracked: an icon, a name, when it began, and every slip since. */
public class Tracker {

    public String id;
    public String icon;
    public String name;

    /** When tracking began. Never moves — a reset appends to {@link #relapses}. */
    public long startMillis;

    /**
     * Every moment the addiction was acted on, ascending.
     *
     * <p>Keeping these instead of overwriting the start date is what makes a
     * reset undoable: delete the entry and the earlier streak comes back.
     */
    public final List<Long> relapses = new ArrayList<Long>();

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
        JSONArray slips = new JSONArray();
        for (Long when : relapses) slips.put(when.longValue());
        o.put("relapses", slips);
        return o;
    }

    public static Tracker fromJson(JSONObject o) {
        Tracker t = new Tracker(
                o.optString("id", String.valueOf(System.nanoTime())),
                o.optString("icon", "✨"),
                o.optString("name", ""),
                o.optLong("start", System.currentTimeMillis()));
        JSONArray slips = o.optJSONArray("relapses");
        if (slips != null) {
            for (int i = 0; i < slips.length(); i++) {
                long when = slips.optLong(i, 0L);
                if (when > 0) t.relapses.add(Long.valueOf(when));
            }
        }
        t.sortRelapses();
        return t;
    }

    /** The moment the current streak began: the last slip, or the original start. */
    public long currentStart() {
        long start = startMillis;
        for (Long when : relapses) {
            if (when.longValue() > start) start = when.longValue();
        }
        return start;
    }

    /** Milliseconds of the current streak; negative if it starts in the future. */
    public long elapsed() {
        return System.currentTimeMillis() - currentStart();
    }

    public void recordRelapse(long when) {
        relapses.add(Long.valueOf(when));
        sortRelapses();
    }

    /** Newest first, which is the order both the history list and reading want. */
    public List<Long> relapsesNewestFirst() {
        List<Long> copy = new ArrayList<Long>(relapses);
        Collections.reverse(copy);
        return copy;
    }

    private void sortRelapses() {
        Collections.sort(relapses);
    }
}
