package com.oliverstoll.sobriety;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/** Widget appearance, edited over a live preview of a widget row. */
public class SettingsActivity extends Activity {

    private static final int BACKGROUND = 0;
    private static final int TEXT = 1;
    private static final int VALUE = 2;

    /** Working copy of everything the form edits. */
    private final Settings.Values look = new Settings.Values();

    private SeekBar textSeek;
    private SeekBar unitSeek;
    private SeekBar padVSeek;
    private SeekBar padHSeek;
    private TextView textLabel;
    private TextView unitLabel;
    private TextView padVLabel;
    private TextView padHLabel;
    private RadioGroup formatGroup;
    private ImageView previewBg;
    private View previewContent;
    private View rowOne;
    private View rowTwo;
    private final View[] swatches = new View[3];

    private List<Tracker> trackers;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_settings);

        trackers = Store.load(this);

        previewBg = (ImageView) findViewById(R.id.preview_bg);
        previewContent = findViewById(R.id.preview_content);
        rowOne = findViewById(R.id.preview_row_one);
        rowTwo = findViewById(R.id.preview_row_two);
        textLabel = (TextView) findViewById(R.id.label_text_size);
        unitLabel = (TextView) findViewById(R.id.label_unit_size);
        padVLabel = (TextView) findViewById(R.id.label_padding_v);
        padHLabel = (TextView) findViewById(R.id.label_padding_h);
        textSeek = (SeekBar) findViewById(R.id.seek_text_size);
        unitSeek = (SeekBar) findViewById(R.id.seek_unit_size);
        padVSeek = (SeekBar) findViewById(R.id.seek_padding_v);
        padHSeek = (SeekBar) findViewById(R.id.seek_padding_h);
        formatGroup = (RadioGroup) findViewById(R.id.format_group);

        copyInto(look, Settings.load(this));
        buildColorRows();

        textSeek.setMax(Settings.MAX_TEXT_SP - Settings.MIN_TEXT_SP);
        unitSeek.setMax(Settings.MAX_UNIT_SP - Settings.MIN_UNIT_SP);
        padVSeek.setMax(Settings.MAX_PADDING_DP - Settings.MIN_PADDING_DP);
        padHSeek.setMax(Settings.MAX_PADDING_DP - Settings.MIN_PADDING_DP);

        SeekBar.OnSeekBarChangeListener live = new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar bar, int value, boolean fromUser) {
                refreshPreview();
            }
            @Override public void onStartTrackingTouch(SeekBar bar) {}
            @Override public void onStopTrackingTouch(SeekBar bar) {
                // Commit once the finger lifts, not on every pixel of the drag.
                applyNow();
            }
        };
        textSeek.setOnSeekBarChangeListener(live);
        unitSeek.setOnSeekBarChangeListener(live);
        padVSeek.setOnSeekBarChangeListener(live);
        padHSeek.setOnSeekBarChangeListener(live);

        formatGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(RadioGroup group, int checkedId) {
                refreshPreview();
                applyNow();
            }
        });

        findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        findViewById(R.id.reset).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copyInto(look, new Settings.Values());
                fillForm();
                refreshPreview();
                applyNow();
            }
        });

        fillForm();
        refreshPreview();
    }

    /** Push the working copy into the controls. */
    private void fillForm() {
        textSeek.setProgress(look.textSp - Settings.MIN_TEXT_SP);
        unitSeek.setProgress(look.unitSp - Settings.MIN_UNIT_SP);
        padVSeek.setProgress(look.paddingVerticalDp - Settings.MIN_PADDING_DP);
        padHSeek.setProgress(look.paddingHorizontalDp - Settings.MIN_PADDING_DP);
        formatGroup.check(look.unitMode == Format.MODE_DAYS
                ? R.id.format_days : R.id.format_adaptive);
    }

    /** Pull the controls into the working copy. */
    private void readForm() {
        look.textSp = textSeek.getProgress() + Settings.MIN_TEXT_SP;
        look.unitSp = unitSeek.getProgress() + Settings.MIN_UNIT_SP;
        look.paddingVerticalDp = padVSeek.getProgress() + Settings.MIN_PADDING_DP;
        look.paddingHorizontalDp = padHSeek.getProgress() + Settings.MIN_PADDING_DP;
        look.unitMode = formatGroup.getCheckedRadioButtonId() == R.id.format_days
                ? Format.MODE_DAYS : Format.MODE_ADAPTIVE;
    }

    private static void copyInto(Settings.Values target, Settings.Values source) {
        target.textSp = source.textSp;
        target.unitSp = source.unitSp;
        target.paddingVerticalDp = source.paddingVerticalDp;
        target.paddingHorizontalDp = source.paddingHorizontalDp;
        target.bgColor = source.bgColor;
        target.textColor = source.textColor;
        target.valueColor = source.valueColor;
        target.unitMode = source.unitMode;
    }

    /**
     * Writes the form straight through to storage, which repaints the widget.
     *
     * <p>There is no Save step: a settings screen that stages changes behind a
     * button loses them silently when the user backs out, and the whole point
     * of the preview is that what you see is already what the widget shows.
     */
    private void applyNow() {
        readForm();
        Settings.save(this, look);
    }

    private void buildColorRows() {
        LinearLayout container = (LinearLayout) findViewById(R.id.color_rows);
        addColorRow(container, BACKGROUND, R.string.settings_color_bg, true);
        addColorRow(container, TEXT, R.string.settings_color_text, false);
        addColorRow(container, VALUE, R.string.settings_color_value, false);
    }

    private void addColorRow(LinearLayout container, final int slot, int labelRes,
                             final boolean allowAlpha) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(8), 0, dp(8));

        TextView label = new TextView(this);
        label.setText(labelRes);
        label.setTextSize(15);
        label.setTextColor(getResources().getColor(R.color.text));
        label.setLayoutParams(new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(label);

        final View swatch = new View(this);
        swatch.setLayoutParams(new LinearLayout.LayoutParams(dp(64), dp(34)));
        row.addView(swatch);
        swatches[slot] = swatch;

        final String title = getString(labelRes);
        row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ColorPicker.show(SettingsActivity.this, title, colorAt(slot), allowAlpha,
                        new ColorPicker.OnPicked() {
                            @Override
                            public void onPicked(int color) {
                                setColorAt(slot, color);
                                refreshPreview();
                                applyNow();
                            }
                        });
            }
        });
        container.addView(row);
    }

    private int colorAt(int slot) {
        if (slot == BACKGROUND) return look.bgColor;
        return slot == TEXT ? look.textColor : look.valueColor;
    }

    private void setColorAt(int slot, int color) {
        if (slot == BACKGROUND) look.bgColor = color;
        else if (slot == TEXT) look.textColor = color;
        else look.valueColor = color;
    }

    private void refreshPreview() {
        readForm();
        int mode = look.unitMode;

        textLabel.setText(getString(R.string.settings_text_size, look.textSp));
        unitLabel.setText(getString(R.string.settings_unit_size, look.unitSp));
        padVLabel.setText(getString(R.string.settings_padding_v, look.paddingVerticalDp));
        padHLabel.setText(getString(R.string.settings_padding_h, look.paddingHorizontalDp));

        for (int slot = 0; slot < swatches.length; slot++) {
            paintSwatch(swatches[slot], colorAt(slot));
        }

        // Same two-step tint the widget uses, so the preview cannot drift.
        previewBg.setColorFilter(Settings.opaque(look.bgColor));
        previewBg.setImageAlpha(Color.alpha(look.bgColor));

        previewContent.setPadding(dp(look.outerPaddingHorizontalDp()),
                dp(look.outerPaddingVerticalDp()),
                dp(look.outerPaddingHorizontalDp()),
                dp(look.outerPaddingVerticalDp()));

        // Measure both preview rows the same way the widget measures all of
        // its rows, so the preview shows the real column alignment.
        List<String> values = new ArrayList<String>();
        List<String> units = new ArrayList<String>();
        for (int i = 0; i < 2; i++) {
            long elapsed = sampleElapsed(i);
            values.add(Format.value(elapsed, mode));
            units.add(Format.unit(elapsed, mode));
        }
        int valueWidth = Columns.width(this, values, look.textSp, true);
        int unitWidth = Columns.width(this, units, look.unitSp, false);

        styleRow(rowOne, 0, mode, valueWidth, unitWidth);
        styleRow(rowTwo, 1, mode, valueWidth, unitWidth);
    }

    private void paintSwatch(View swatch, int color) {
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(color);
        shape.setCornerRadius(dp(8));
        shape.setStroke(dp(1), 0x44FFFFFF);
        swatch.setBackground(shape);
    }

    /** Stand-ins show a long and a short counter, so both units are visible. */
    private long sampleElapsed(int index) {
        if (index < trackers.size()) return trackers.get(index).elapsed();
        return index == 0 ? 128 * Format.DAY : 5 * Format.HOUR;
    }

    /** Real counters when there are any, otherwise a plausible stand-in. */
    private void styleRow(View row, int index, int mode, int valueWidth, int unitWidth) {
        String icon = index == 0 ? "🍺" : "🚬";
        String name = index == 0 ? "Alcohol" : "Nicotine";
        long elapsed = sampleElapsed(index);
        if (index < trackers.size()) {
            Tracker t = trackers.get(index);
            icon = t.icon;
            name = t.name;
        }

        int vPad = dp(look.paddingVerticalDp);
        int hPad = dp(look.paddingHorizontalDp);
        row.setPadding(hPad, vPad, hPad, vPad);

        TextView iconView = (TextView) row.findViewById(R.id.w_icon);
        TextView nameView = (TextView) row.findViewById(R.id.w_name);
        TextView valueView = (TextView) row.findViewById(R.id.w_days);
        TextView unitView = (TextView) row.findViewById(R.id.w_unit);

        iconView.setText(icon);
        nameView.setText(name);
        valueView.setText(Format.value(elapsed, mode));
        unitView.setText(Format.unit(elapsed, mode));

        setSp(iconView, look.iconSp());
        setSp(nameView, look.textSp);
        setSp(valueView, look.textSp);
        setSp(unitView, look.unitSp);

        valueView.setMinWidth(valueWidth);
        unitView.setMinWidth(unitWidth);

        nameView.setTextColor(look.textColor);
        valueView.setTextColor(look.valueColor);
        unitView.setTextColor(look.unitColor());
    }

    private void setSp(TextView view, float sp) {
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
    }

    private int dp(int value) {
        return Settings.dpToPx(this, value);
    }
}
