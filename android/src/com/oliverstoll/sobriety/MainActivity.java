package com.oliverstoll.sobriety;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TimePicker;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class MainActivity extends Activity {

    private static final String[] SUGGESTED_ICONS = {
            "🍺", "🚬", "🍩", "🎰", "📱", "☕", "🍷", "💊", "🥤", "🎮", "🛒", "✨"
    };

    /** Keeps a minutes-old counter ticking while the list is on screen. */
    private static final long TICK_MS = 20000L;

    private final Handler ticker = new Handler();
    private final Runnable tick = new Runnable() {
        @Override
        public void run() {
            adapter.notifyDataSetChanged();
            ticker.postDelayed(this, TICK_MS);
        }
    };

    private List<Tracker> trackers;
    private Adapter adapter;
    private ListView list;
    private View empty;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_main);

        trackers = Store.load(this);
        list = (ListView) findViewById(R.id.list);
        empty = findViewById(R.id.empty);
        adapter = new Adapter();
        list.setAdapter(adapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                edit(position);
            }
        });

        findViewById(R.id.add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edit(-1);
            }
        });

        findViewById(R.id.settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            }
        });

        refresh();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Time has passed while the app sat in the background.
        trackers = Store.load(this);
        refresh();
        ticker.postDelayed(tick, TICK_MS);
    }

    @Override
    protected void onPause() {
        super.onPause();
        ticker.removeCallbacks(tick);
    }

    private void refresh() {
        adapter.notifyDataSetChanged();
        boolean none = trackers.isEmpty();
        empty.setVisibility(none ? View.VISIBLE : View.GONE);
        list.setVisibility(none ? View.GONE : View.VISIBLE);
    }

    private void persist() {
        Store.save(this, trackers);
        refresh();
    }

    private void confirmReset(final Tracker tracker) {
        Reset.confirm(this, tracker, new Reset.OnDone() {
            @Override
            public void onDone(boolean didReset) {
                if (didReset) persist();
            }
        });
    }

    /** position < 0 adds a new tracker, otherwise edits an existing one. */
    private void edit(final int position) {
        final boolean isNew = position < 0;
        final Tracker existing = isNew ? null : trackers.get(position);

        View form = LayoutInflater.from(this).inflate(R.layout.dialog_edit, null);
        // Staged like the other fields: edits here only land on Save.
        final List<Long> slips = new ArrayList<Long>(
                isNew ? new ArrayList<Long>() : existing.relapses);
        final EditText nameIn = (EditText) form.findViewById(R.id.in_name);
        final EditText iconIn = (EditText) form.findViewById(R.id.in_icon);
        final Button dateBtn = (Button) form.findViewById(R.id.in_date);
        final Button timeBtn = (Button) form.findViewById(R.id.in_time);
        LinearLayout picker = (LinearLayout) form.findViewById(R.id.icon_picker);

        // A new counter starts now, to the minute — that is what makes the
        // first hours readable instead of sitting at "0 days" until midnight.
        final long[] chosen = { isNew ? System.currentTimeMillis() : existing.startMillis };
        nameIn.setText(isNew ? "" : existing.name);
        iconIn.setText(isNew ? SUGGESTED_ICONS[0] : existing.icon);
        dateBtn.setText(Days.formatDate(chosen[0]));
        timeBtn.setText(Days.formatTime(chosen[0]));

        for (int i = 0; i < SUGGESTED_ICONS.length; i++) {
            final String emoji = SUGGESTED_ICONS[i];
            TextView chip = new TextView(this);
            chip.setText(emoji);
            chip.setTextSize(22);
            chip.setPadding(dp(10), dp(6), dp(10), dp(6));
            chip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    iconIn.setText(emoji);
                }
            });
            picker.addView(chip);
        }

        dateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog dlg = new DatePickerDialog(MainActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int y, int m, int d) {
                                // Keep the time of day; only the date moves.
                                chosen[0] = Days.withDate(chosen[0], y, m, d);
                                dateBtn.setText(Days.formatDate(chosen[0]));
                                timeBtn.setText(Days.formatTime(chosen[0]));
                            }
                        },
                        Days.field(chosen[0], Calendar.YEAR),
                        Days.field(chosen[0], Calendar.MONTH),
                        Days.field(chosen[0], Calendar.DAY_OF_MONTH));
                dlg.getDatePicker().setMaxDate(System.currentTimeMillis());
                dlg.show();
            }
        });

        timeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TimePickerDialog(MainActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hour, int minute) {
                                chosen[0] = Days.withTime(chosen[0], hour, minute);
                                timeBtn.setText(Days.formatTime(chosen[0]));
                            }
                        },
                        Days.field(chosen[0], Calendar.HOUR_OF_DAY),
                        Days.field(chosen[0], Calendar.MINUTE),
                        android.text.format.DateFormat.is24HourFormat(MainActivity.this))
                        .show();
            }
        });

        renderHistory(form, slips);

        AlertDialog.Builder b = new AlertDialog.Builder(this)
                .setTitle(isNew ? "New counter" : "Edit counter")
                .setView(form)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int which) {
                        String name = nameIn.getText().toString().trim();
                        String icon = iconIn.getText().toString().trim();
                        if (TextUtils.isEmpty(name)) name = "Untitled";
                        if (TextUtils.isEmpty(icon)) icon = "✨";
                        if (isNew) {
                            trackers.add(new Tracker(
                                    String.valueOf(System.currentTimeMillis()),
                                    icon, name, chosen[0]));
                        } else {
                            existing.name = name;
                            existing.icon = icon;
                            existing.startMillis = chosen[0];
                            existing.relapses.clear();
                            existing.relapses.addAll(slips);
                        }
                        persist();
                    }
                });

        if (!isNew) {
            b.setNeutralButton("Delete", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface d, int which) {
                    confirmDelete(position);
                }
            });
        }
        b.show();
    }

    private void confirmDelete(final int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete “" + trackers.get(position).name + "”?")
                .setMessage("This removes the counter and its start date.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int which) {
                        trackers.remove(position);
                        persist();
                    }
                })
                .show();
    }

    /** Lists every recorded slip, newest first, each removable. */
    private void renderHistory(final View form, final List<Long> slips) {
        TextView label = (TextView) form.findViewById(R.id.history_label);
        LinearLayout container = (LinearLayout) form.findViewById(R.id.history);
        container.removeAllViews();

        if (slips.isEmpty()) {
            label.setText(R.string.history_none);
            return;
        }
        label.setText(getString(R.string.history_some, slips.size()));

        List<Long> newestFirst = new ArrayList<Long>(slips);
        Collections.sort(newestFirst);
        Collections.reverse(newestFirst);

        for (int i = 0; i < newestFirst.size(); i++) {
            final Long when = newestFirst.get(i);

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(0, dp(6), 0, dp(6));

            TextView stamp = new TextView(this);
            stamp.setText(Days.formatDateTime(when.longValue()));
            stamp.setTextSize(14);
            stamp.setTextColor(getResources().getColor(R.color.text));
            stamp.setLayoutParams(new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            row.addView(stamp);

            TextView remove = new TextView(this);
            remove.setText("✕");
            remove.setTextSize(16);
            remove.setTextColor(getResources().getColor(R.color.text_dim));
            remove.setPadding(dp(14), dp(2), dp(4), dp(2));
            remove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    slips.remove(when);
                    renderHistory(form, slips);
                }
            });
            row.addView(remove);

            container.addView(row);
        }
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private class Adapter extends BaseAdapter {

        @Override public int getCount() { return trackers.size(); }

        @Override public Object getItem(int position) { return trackers.get(position); }

        @Override public long getItemId(int position) { return position; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                v = LayoutInflater.from((Context) MainActivity.this)
                        .inflate(R.layout.row, parent, false);
            }
            final Tracker t = trackers.get(position);
            long elapsed = t.elapsed();
            int mode = Settings.unitMode(MainActivity.this);
            TextView iconView = (TextView) v.findViewById(R.id.icon);
            iconView.setText(t.icon);
            // The icon resets, matching the widget. The rest of the row edits.
            iconView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    confirmReset(t);
                }
            });
            ((TextView) v.findViewById(R.id.name)).setText(t.name);
            ((TextView) v.findViewById(R.id.since)).setText(Days.startedAt(t.startMillis));
            TextView dayView = (TextView) v.findViewById(R.id.days);
            dayView.setText(Format.value(elapsed, mode));
            dayView.setTextColor(elapsed < 0 ? Color.parseColor("#93A1B0")
                                             : getResources().getColor(R.color.accent));
            ((TextView) v.findViewById(R.id.days_label)).setText(Format.unit(elapsed, mode));
            return v;
        }
    }
}
