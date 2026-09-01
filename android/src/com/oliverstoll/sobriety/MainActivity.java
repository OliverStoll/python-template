package com.oliverstoll.sobriety;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
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
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Calendar;
import java.util.List;

public class MainActivity extends Activity {

    private static final String[] SUGGESTED_ICONS = {
            "🍺", "🚬", "🍩", "🎰", "📱", "☕", "🍷", "💊", "🥤", "🎮", "🛒", "✨"
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
                showSettings();
            }
        });

        refresh();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // The day may have rolled over while the app sat in the background.
        trackers = Store.load(this);
        refresh();
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

    /** position < 0 adds a new tracker, otherwise edits an existing one. */
    private void edit(final int position) {
        final boolean isNew = position < 0;
        final Tracker existing = isNew ? null : trackers.get(position);

        View form = LayoutInflater.from(this).inflate(R.layout.dialog_edit, null);
        final EditText nameIn = (EditText) form.findViewById(R.id.in_name);
        final EditText iconIn = (EditText) form.findViewById(R.id.in_icon);
        final Button dateBtn = (Button) form.findViewById(R.id.in_date);
        LinearLayout picker = (LinearLayout) form.findViewById(R.id.icon_picker);

        final long[] chosen = { isNew ? Days.startOfDay(System.currentTimeMillis())
                                      : existing.startMillis };
        nameIn.setText(isNew ? "" : existing.name);
        iconIn.setText(isNew ? SUGGESTED_ICONS[0] : existing.icon);
        dateBtn.setText(Days.format(chosen[0]));

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
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(chosen[0]);
                DatePickerDialog dlg = new DatePickerDialog(MainActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int y, int m, int d) {
                                Calendar picked = Calendar.getInstance();
                                picked.set(y, m, d, 0, 0, 0);
                                picked.set(Calendar.MILLISECOND, 0);
                                chosen[0] = picked.getTimeInMillis();
                                dateBtn.setText(Days.format(chosen[0]));
                            }
                        },
                        c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
                dlg.getDatePicker().setMaxDate(System.currentTimeMillis());
                dlg.show();
            }
        });

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

    /** Two sliders over a live copy of a widget row. */
    private void showSettings() {
        View form = LayoutInflater.from(this).inflate(R.layout.dialog_settings, null);

        final View previewRoot = form.findViewById(R.id.preview_root);
        final View rowOne = form.findViewById(R.id.preview_row_one);
        final View rowTwo = form.findViewById(R.id.preview_row_two);
        final TextView textLabel = (TextView) form.findViewById(R.id.label_text_size);
        final TextView padLabel = (TextView) form.findViewById(R.id.label_row_padding);
        final SeekBar textSeek = (SeekBar) form.findViewById(R.id.seek_text_size);
        final SeekBar padSeek = (SeekBar) form.findViewById(R.id.seek_row_padding);

        fillPreviewRow(rowOne, 0);
        fillPreviewRow(rowTwo, 1);

        textSeek.setMax(Settings.MAX_TEXT_SP - Settings.MIN_TEXT_SP);
        padSeek.setMax(Settings.MAX_ROW_PADDING_DP - Settings.MIN_ROW_PADDING_DP);

        final Runnable apply = new Runnable() {
            @Override
            public void run() {
                int textSp = textSeek.getProgress() + Settings.MIN_TEXT_SP;
                int padDp = padSeek.getProgress() + Settings.MIN_ROW_PADDING_DP;
                textLabel.setText(getString(R.string.settings_text_size, textSp));
                padLabel.setText(getString(R.string.settings_row_padding, padDp));
                stylePreviewRow(rowOne, textSp, padDp);
                stylePreviewRow(rowTwo, textSp, padDp);
                int outer = Settings.dpToPx(MainActivity.this, Settings.outerPaddingDp(padDp));
                previewRoot.setPadding(outer, outer, outer, outer);
            }
        };

        SeekBar.OnSeekBarChangeListener live = new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar bar, int value, boolean fromUser) {
                apply.run();
            }
            @Override public void onStartTrackingTouch(SeekBar bar) {}
            @Override public void onStopTrackingTouch(SeekBar bar) {}
        };
        textSeek.setOnSeekBarChangeListener(live);
        padSeek.setOnSeekBarChangeListener(live);

        textSeek.setProgress(Settings.textSp(this) - Settings.MIN_TEXT_SP);
        padSeek.setProgress(Settings.rowPaddingDp(this) - Settings.MIN_ROW_PADDING_DP);
        apply.run();

        new AlertDialog.Builder(this)
                .setTitle(R.string.settings_title)
                .setView(form)
                .setNegativeButton("Cancel", null)
                .setNeutralButton(R.string.settings_reset, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int which) {
                        Settings.save(MainActivity.this,
                                Settings.DEFAULT_TEXT_SP, Settings.DEFAULT_ROW_PADDING_DP);
                    }
                })
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int which) {
                        Settings.save(MainActivity.this,
                                textSeek.getProgress() + Settings.MIN_TEXT_SP,
                                padSeek.getProgress() + Settings.MIN_ROW_PADDING_DP);
                    }
                })
                .show();
    }

    /** Show a real counter in the preview when there is one, else a stand-in. */
    private void fillPreviewRow(View row, int index) {
        String icon = index == 0 ? "\ud83c\udf7a" : "\ud83d\udeac";
        String name = index == 0 ? "Alcohol" : "Nicotine";
        String days = index == 0 ? "128d" : "41d";
        if (index < trackers.size()) {
            Tracker t = trackers.get(index);
            icon = t.icon;
            name = t.name;
            days = t.days() < 0 ? "\u2014" : t.days() + "d";
        }
        ((TextView) row.findViewById(R.id.w_icon)).setText(icon);
        ((TextView) row.findViewById(R.id.w_name)).setText(name);
        ((TextView) row.findViewById(R.id.w_days)).setText(days);
    }

    private void stylePreviewRow(View row, int textSp, int padDp) {
        int vPad = Settings.dpToPx(this, padDp);
        int hPad = Settings.dpToPx(this, Settings.rowPaddingHorizontalDp(padDp));
        row.setPadding(hPad, vPad, hPad, vPad);
        setSp(row.findViewById(R.id.w_icon), Settings.iconSp(textSp));
        setSp(row.findViewById(R.id.w_name), textSp);
        setSp(row.findViewById(R.id.w_days), textSp);
    }

    private void setSp(View view, float sp) {
        ((TextView) view).setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
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
            Tracker t = trackers.get(position);
            int days = t.days();
            ((TextView) v.findViewById(R.id.icon)).setText(t.icon);
            ((TextView) v.findViewById(R.id.name)).setText(t.name);
            ((TextView) v.findViewById(R.id.since)).setText(Days.humanSince(t.startMillis));
            TextView dayView = (TextView) v.findViewById(R.id.days);
            dayView.setText(days < 0 ? "—" : String.valueOf(days));
            dayView.setTextColor(days < 0 ? Color.parseColor("#93A1B0")
                                          : getResources().getColor(R.color.accent));
            ((TextView) v.findViewById(R.id.days_label))
                    .setText(Math.abs(days) == 1 ? "day" : "days");
            return v;
        }
    }
}
