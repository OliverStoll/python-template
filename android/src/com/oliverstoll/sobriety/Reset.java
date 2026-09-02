package com.oliverstoll.sobriety;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import java.util.List;

/** The one confirmation prompt behind every reset, wherever it is triggered. */
public final class Reset {

    public interface OnDone {
        void onDone(boolean didReset);
    }

    private Reset() {}

    /**
     * Asks first, then records a slip at the current moment and restarts the
     * counter from there. Always asks: a reset throws away a streak, and both
     * entry points are a single tap on a small target.
     */
    public static void confirm(Context ctx, final Tracker tracker, final OnDone done) {
        // No explicit theme: the builder resolves alertDialogTheme from the
        // host, so the app gets the dimmed card and the widget's invisible
        // activity gets the same card with nothing behind it.
        new AlertDialog.Builder(ctx)
                .setTitle("Reset " + tracker.name + "?")
                .setMessage("This records a slip right now and starts the count again from"
                        + " this moment.\n\nThe current streak began "
                        + Days.formatDateTime(tracker.currentStart()) + ".")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int which) {
                        if (done != null) done.onDone(false);
                    }
                })
                .setPositiveButton("Reset", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int which) {
                        tracker.recordRelapse(System.currentTimeMillis());
                        if (done != null) done.onDone(true);
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface d) {
                        if (done != null) done.onDone(false);
                    }
                })
                .show();
    }

    public static Tracker find(List<Tracker> trackers, String id) {
        if (id == null) return null;
        for (Tracker t : trackers) {
            if (id.equals(t.id)) return t;
        }
        return null;
    }
}
