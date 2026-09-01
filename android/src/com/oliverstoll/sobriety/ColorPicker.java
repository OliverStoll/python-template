package com.oliverstoll.sobriety;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

/** A swatch grid, an opacity slider and a hex field, built without a layout file. */
public final class ColorPicker {

    public interface OnPicked {
        void onPicked(int color);
    }

    private static final int[] SWATCHES = {
            0xFF10151C, 0xFF1B2430, 0xFF2E3440, 0xFF000000,
            0xFFFFFFFF, 0xFFF2F5F8, 0xFF93A1B0, 0xFF5B6673,
            0xFF4ADE80, 0xFF22D3EE, 0xFF60A5FA, 0xFFA78BFA,
            0xFFF472B6, 0xFFFB7185, 0xFFFBBF24, 0xFFF97316,
    };

    private ColorPicker() {}

    public static void show(final Activity activity, String title, int initial,
                            final boolean allowAlpha, final OnPicked callback) {

        int pad = Settings.dpToPx(activity, 20);
        LinearLayout root = new LinearLayout(activity);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad, Settings.dpToPx(activity, 8), pad, 0);

        final View preview = new View(activity);
        LinearLayout.LayoutParams previewParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, Settings.dpToPx(activity, 52));
        preview.setLayoutParams(previewParams);
        root.addView(preview);

        final EditText hex = new EditText(activity);
        hex.setSingleLine(true);
        hex.setFilters(new InputFilter[] { new InputFilter.LengthFilter(9) });
        hex.setTextSize(16);

        final SeekBar alpha = new SeekBar(activity);
        alpha.setMax(255);

        // One shared writer so swatch, slider and hex field never fight.
        final int[] current = { initial };
        final boolean[] editingHex = { false };

        final Runnable redraw = new Runnable() {
            @Override
            public void run() {
                GradientDrawable shape = new GradientDrawable();
                shape.setColor(current[0]);
                shape.setCornerRadius(Settings.dpToPx(activity, 10));
                shape.setStroke(Settings.dpToPx(activity, 1), 0x44FFFFFF);
                preview.setBackground(shape);
                if (!editingHex[0]) {
                    hex.setText(toHex(current[0], allowAlpha));
                }
                if (allowAlpha && alpha.getProgress() != Color.alpha(current[0])) {
                    alpha.setProgress(Color.alpha(current[0]));
                }
            }
        };

        LinearLayout grid = new LinearLayout(activity);
        grid.setOrientation(LinearLayout.VERTICAL);
        grid.setPadding(0, Settings.dpToPx(activity, 12), 0, 0);
        int perRow = 4;
        LinearLayout row = null;
        for (int i = 0; i < SWATCHES.length; i++) {
            if (i % perRow == 0) {
                row = new LinearLayout(activity);
                row.setOrientation(LinearLayout.HORIZONTAL);
                grid.addView(row);
            }
            final int swatch = SWATCHES[i];
            View cell = new View(activity);
            LinearLayout.LayoutParams cellParams = new LinearLayout.LayoutParams(
                    0, Settings.dpToPx(activity, 44), 1f);
            int gap = Settings.dpToPx(activity, 3);
            cellParams.setMargins(gap, gap, gap, gap);
            cell.setLayoutParams(cellParams);

            GradientDrawable shape = new GradientDrawable();
            shape.setColor(swatch);
            shape.setCornerRadius(Settings.dpToPx(activity, 8));
            shape.setStroke(Settings.dpToPx(activity, 1), 0x44FFFFFF);
            cell.setBackground(shape);

            cell.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Keep the opacity the user already chose.
                    current[0] = allowAlpha
                            ? Settings.withAlpha(swatch, Color.alpha(current[0]))
                            : Settings.opaque(swatch);
                    editingHex[0] = false;
                    redraw.run();
                }
            });
            row.addView(cell);
        }
        root.addView(grid);

        if (allowAlpha) {
            TextView label = new TextView(activity);
            label.setText("Opacity");
            label.setTextSize(12);
            label.setPadding(0, Settings.dpToPx(activity, 12), 0, 0);
            root.addView(label);

            alpha.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar bar, int value, boolean fromUser) {
                    if (!fromUser) return;
                    current[0] = Settings.withAlpha(current[0], value);
                    editingHex[0] = false;
                    redraw.run();
                }
                @Override public void onStartTrackingTouch(SeekBar bar) {}
                @Override public void onStopTrackingTouch(SeekBar bar) {}
            });
            root.addView(alpha);
        }

        TextView hexLabel = new TextView(activity);
        hexLabel.setText(allowAlpha ? "Hex (#AARRGGBB or #RRGGBB)" : "Hex (#RRGGBB)");
        hexLabel.setTextSize(12);
        hexLabel.setPadding(0, Settings.dpToPx(activity, 10), 0, 0);
        root.addView(hexLabel);

        hex.setGravity(Gravity.START);
        hex.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override
            public void afterTextChanged(Editable s) {
                Integer parsed = parseHex(s.toString(), allowAlpha, Color.alpha(current[0]));
                if (parsed == null || parsed.intValue() == current[0]) return;
                current[0] = parsed.intValue();
                editingHex[0] = true;
                redraw.run();
                editingHex[0] = false;
            }
        });
        root.addView(hex);

        redraw.run();

        new AlertDialog.Builder(activity)
                .setTitle(title)
                .setView(root)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Select", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int which) {
                        callback.onPicked(current[0]);
                    }
                })
                .show();
    }

    static String toHex(int color, boolean withAlpha) {
        if (withAlpha) {
            return String.format("#%08X", Integer.valueOf(color));
        }
        return String.format("#%06X", Integer.valueOf(color & 0x00FFFFFF));
    }

    /** Returns null for anything not yet a complete colour, so typing is not fought. */
    static Integer parseHex(String raw, boolean allowAlpha, int fallbackAlpha) {
        String text = raw.trim();
        if (text.startsWith("#")) text = text.substring(1);
        if (text.length() != 6 && text.length() != 8) return null;
        long value;
        try {
            value = Long.parseLong(text, 16);
        } catch (NumberFormatException e) {
            return null;
        }
        if (text.length() == 6) {
            int alpha = allowAlpha ? fallbackAlpha : 255;
            return Integer.valueOf(Settings.withAlpha((int) value, alpha));
        }
        int color = (int) value;
        return Integer.valueOf(allowAlpha ? color : Settings.opaque(color));
    }
}
