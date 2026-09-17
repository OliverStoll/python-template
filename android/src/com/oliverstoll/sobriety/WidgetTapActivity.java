package com.oliverstoll.sobriety;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import java.util.List;

/**
 * Where every widget tap lands.
 *
 * <p>A collection widget gets one PendingIntent template for the whole list, so
 * the row and the icon cannot target different activities. They send different
 * fill-in extras to this one instead, which either resets that counter or opens
 * the app. It is translucent, so a reset never leaves the home screen.
 */
public class WidgetTapActivity extends Activity {

    static final String EXTRA_ACTION = "com.oliverstoll.sobriety.ACTION";
    static final String EXTRA_TRACKER_ID = "com.oliverstoll.sobriety.TRACKER_ID";
    static final String ACTION_RESET = "reset";

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        Intent intent = getIntent();
        boolean isReset = intent != null
                && ACTION_RESET.equals(intent.getStringExtra(EXTRA_ACTION));
        if (!isReset) {
            openApp();
            return;
        }

        final List<Tracker> trackers = Store.load(this);
        final Tracker tracker = Reset.find(trackers, intent.getStringExtra(EXTRA_TRACKER_ID));
        if (tracker == null) {
            // Deleted between the widget drawing and the tap landing.
            openApp();
            return;
        }

        Reset.confirm(this, tracker, new Reset.OnDone() {
            @Override
            public void onDone(boolean didReset) {
                if (didReset) {
                    Store.save(WidgetTapActivity.this, trackers);
                    Toast.makeText(WidgetTapActivity.this,
                            tracker.name + " reset", Toast.LENGTH_SHORT).show();
                }
                finish();
            }
        });
    }

    private void openApp() {
        startActivity(new Intent(this, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
        finish();
    }
}
