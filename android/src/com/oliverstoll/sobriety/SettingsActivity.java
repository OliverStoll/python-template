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

import java.util.List;

/** Widget appearance, edited over a live preview of a widget row. */
public class SettingsActivity extends Activity {

    private static final int BACKGROUND = 0;
    private static final int TEXT = 1;
    private static final int VALUE = 2;

    /** Working copy; only written back to storage on Save. */
    private final int[] colors = new int[3];

    private SeekBar textSeek;
    private SeekBar padSeek;
    private TextView textLabel;
    private TextView padLabel;
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
        padLabel = (TextView) findViewById(R.id.label_row_padding);
        textSeek = (SeekBar) findViewById(R.id.seek_text_size);
        padSeek = (SeekBar) findViewById(R.id.seek_row_padding);
        formatGroup = (RadioGroup) findViewById(R.id.format_group);

        colors[BACKGROUND] = Settings.bgColor(this);
        colors[TEXT] = Settings.textColor(this);
        colors[VALUE] = Settings.valueColor(this);

        buildColorRows();

        textSeek.setMax(Settings.MAX_TEXT_SP - Settings.MIN_TEXT_SP);
        padSeek.setMax(Settings.MAX_ROW_PADDING_DP - Settings.MIN_ROW_PADDING_DP);

        SeekBar.OnSeekBarChangeListener live = new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar bar, int value, boolean fromUser) {
                refreshPreview();
            }
            @Override public void onStartTrackingTouch(SeekBar bar) {}
            @Override public void onStopTrackingTouch(SeekBar bar) {}
        };
        textSeek.setOnSeekBarChangeListener(live);
        padSeek.setOnSeekBarChangeListener(live);

        formatGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(RadioGroup group, int checkedId) {
                refreshPreview();
            }
        });

        findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Settings.save(SettingsActivity.this,
                        chosenTextSp(), chosenPadDp(),
                        colors[BACKGROUND], colors[TEXT], colors[VALUE], chosenMode());
                finish();
            }
        });

        findViewById(R.id.reset).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colors[BACKGROUND] = Settings.DEFAULT_BG_COLOR;
                colors[TEXT] = Settings.DEFAULT_TEXT_COLOR;
                colors[VALUE] = Settings.DEFAULT_VALUE_COLOR;
                textSeek.setProgress(Settings.DEFAULT_TEXT_SP - Settings.MIN_TEXT_SP);
                padSeek.setProgress(Settings.DEFAULT_ROW_PADDING_DP - Settings.MIN_ROW_PADDING_DP);
                formatGroup.check(Settings.DEFAULT_UNIT_MODE == Format.MODE_DAYS
                        ? R.id.format_days : R.id.format_adaptive);
                refreshPreview();
            }
        });

        textSeek.setProgress(Settings.textSp(this) - Settings.MIN_TEXT_SP);
        padSeek.setProgress(Settings.rowPaddingDp(this) - Settings.MIN_ROW_PADDING_DP);
        formatGroup.check(Settings.unitMode(this) == Format.MODE_DAYS
                ? R.id.format_days : R.id.format_adaptive);
        refreshPreview();
    }

    private int chosenTextSp() {
        return textSeek.getProgress() + Settings.MIN_TEXT_SP;
    }

    private int chosenPadDp() {
        return padSeek.getProgress() + Settings.MIN_ROW_PADDING_DP;
    }

    private int chosenMode() {
        return formatGroup.getCheckedRadioButtonId() == R.id.format_days
                ? Format.MODE_DAYS : Format.MODE_ADAPTIVE;
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
                ColorPicker.show(SettingsActivity.this, title, colors[slot], allowAlpha,
                        new ColorPicker.OnPicked() {
                            @Override
                            public void onPicked(int color) {
                                colors[slot] = color;
                                refreshPreview();
                            }
                        });
            }
        });
        container.addView(row);
    }

    private void refreshPreview() {
        int textSp = chosenTextSp();
        int padDp = chosenPadDp();
        int mode = chosenMode();

        textLabel.setText(getString(R.string.settings_text_size, textSp));
        padLabel.setText(getString(R.string.settings_row_padding, padDp));

        for (int slot = 0; slot < swatches.length; slot++) {
            paintSwatch(swatches[slot], colors[slot]);
        }

        // Same two-step tint the widget uses, so the preview cannot drift.
        previewBg.setColorFilter(Settings.opaque(colors[BACKGROUND]));
        previewBg.setImageAlpha(Color.alpha(colors[BACKGROUND]));

        int outer = dp(Settings.outerPaddingDp(padDp));
        previewContent.setPadding(outer, outer, outer, outer);

        styleRow(rowOne, 0, textSp, padDp, mode);
        styleRow(rowTwo, 1, textSp, padDp, mode);
    }

    private void paintSwatch(View swatch, int color) {
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(color);
        shape.setCornerRadius(dp(8));
        shape.setStroke(dp(1), 0x44FFFFFF);
        swatch.setBackground(shape);
    }

    /** Real counters when there are any, otherwise a plausible stand-in. */
    private void styleRow(View row, int index, int textSp, int padDp, int mode) {
        String icon = index == 0 ? "🍺" : "🚬";
        String name = index == 0 ? "Alcohol" : "Nicotine";
        int days = index == 0 ? 128 : 41;
        if (index < trackers.size()) {
            Tracker t = trackers.get(index);
            icon = t.icon;
            name = t.name;
            days = t.days();
        }

        int vPad = dp(padDp);
        int hPad = dp(Settings.rowPaddingHorizontalDp(padDp));
        row.setPadding(hPad, vPad, hPad, vPad);

        TextView iconView = (TextView) row.findViewById(R.id.w_icon);
        TextView nameView = (TextView) row.findViewById(R.id.w_name);
        TextView valueView = (TextView) row.findViewById(R.id.w_days);
        TextView unitView = (TextView) row.findViewById(R.id.w_unit);

        iconView.setText(icon);
        nameView.setText(name);
        valueView.setText(Format.value(days, mode));
        unitView.setText(Format.unit(days, mode));

        setSp(iconView, Settings.iconSp(textSp));
        setSp(nameView, textSp);
        setSp(valueView, textSp);
        setSp(unitView, Settings.unitSp(textSp));

        nameView.setTextColor(colors[TEXT]);
        valueView.setTextColor(colors[VALUE]);
        unitView.setTextColor(Settings.unitColor(colors[TEXT]));
    }

    private void setSp(TextView view, float sp) {
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
    }

    private int dp(int value) {
        return Settings.dpToPx(this, value);
    }
}
