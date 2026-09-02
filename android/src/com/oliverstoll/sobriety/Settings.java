package com.oliverstoll.sobriety;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

/**
 * Everything about how the widget draws itself.
 *
 * <p>All of it is applied at runtime through {@code RemoteViews} — sizes and
 * padding via {@code setTextViewTextSize}, {@code setViewPadding} and
 * {@code setMinWidth}, colours via {@code setTextColor} and a tinted background
 * image — rather than being baked into the layout XML. Saving therefore redraws
 * placed widgets with no reinstall. The layout XML carries the same defaults,
 * which keeps the first frame correct before the adapter runs.
 */
public final class Settings {

    /** Size of a row's name and count, in sp. */
    public static final int DEFAULT_TEXT_SP = 13;
    public static final int MIN_TEXT_SP = 8;
    public static final int MAX_TEXT_SP = 22;

    /** Size of the unit word beside the count, in sp. */
    public static final int DEFAULT_UNIT_SP = 11;
    public static final int MIN_UNIT_SP = 6;
    public static final int MAX_UNIT_SP = 20;

    /** Padding inside a row, in dp. Vertical sets row pitch, horizontal insets it. */
    public static final int DEFAULT_PADDING_V_DP = 3;
    public static final int DEFAULT_PADDING_H_DP = 4;
    public static final int MIN_PADDING_DP = 0;
    public static final int MAX_PADDING_DP = 14;

    public static final int DEFAULT_BG_COLOR = 0xE610151C;
    public static final int DEFAULT_TEXT_COLOR = 0xFFF2F5F8;
    public static final int DEFAULT_VALUE_COLOR = 0xFFF2F5F8;

    public static final int DEFAULT_UNIT_MODE = Format.MODE_ADAPTIVE;

    private static final String KEY_TEXT_SP = "widget_text_sp";
    private static final String KEY_UNIT_SP = "widget_unit_sp";
    private static final String KEY_PADDING_V_DP = "widget_row_padding_dp";
    private static final String KEY_PADDING_H_DP = "widget_row_padding_h_dp";
    private static final String KEY_BG_COLOR = "widget_bg_color";
    private static final String KEY_TEXT_COLOR = "widget_text_color";
    private static final String KEY_VALUE_COLOR = "widget_value_color";
    private static final String KEY_UNIT_MODE = "widget_unit_mode";
    private static final String KEY_SCHEMA = "settings_schema";

    /** Bumped when stored settings need repairing on upgrade. */
    private static final int SCHEMA = 2;

    private Settings() {}

    /** One snapshot of every setting, so nothing is read twice mid-render. */
    public static class Values {
        public int textSp = DEFAULT_TEXT_SP;
        public int unitSp = DEFAULT_UNIT_SP;
        public int paddingVerticalDp = DEFAULT_PADDING_V_DP;
        public int paddingHorizontalDp = DEFAULT_PADDING_H_DP;
        public int bgColor = DEFAULT_BG_COLOR;
        public int textColor = DEFAULT_TEXT_COLOR;
        public int valueColor = DEFAULT_VALUE_COLOR;
        public int unitMode = DEFAULT_UNIT_MODE;

        /** The unit word sits at the same colour as the name, just softer. */
        public int unitColor() {
            return withAlpha(textColor, Math.round(Color.alpha(textColor) * 0.65f));
        }

        /** The emoji reads small next to the text, so give it a couple of points. */
        public float iconSp() {
            return textSp + 2f;
        }

        /** The widget's outer margin, a step wider than the rows sit. */
        public int outerPaddingVerticalDp() {
            return paddingVerticalDp + 3;
        }

        public int outerPaddingHorizontalDp() {
            return paddingHorizontalDp + 2;
        }
    }

    private static SharedPreferences prefs(Context ctx) {
        return Store.prefs(ctx);
    }

    public static Values load(Context ctx) {
        SharedPreferences p = prefs(ctx);
        if (p.getInt(KEY_SCHEMA, 1) < SCHEMA) repair(p);
        Values v = new Values();
        v.textSp = clamp(p.getInt(KEY_TEXT_SP, DEFAULT_TEXT_SP), MIN_TEXT_SP, MAX_TEXT_SP);
        v.unitSp = clamp(p.getInt(KEY_UNIT_SP, DEFAULT_UNIT_SP), MIN_UNIT_SP, MAX_UNIT_SP);
        v.paddingVerticalDp = clamp(p.getInt(KEY_PADDING_V_DP, DEFAULT_PADDING_V_DP),
                MIN_PADDING_DP, MAX_PADDING_DP);
        v.paddingHorizontalDp = clamp(p.getInt(KEY_PADDING_H_DP, DEFAULT_PADDING_H_DP),
                MIN_PADDING_DP, MAX_PADDING_DP);
        v.bgColor = p.getInt(KEY_BG_COLOR, DEFAULT_BG_COLOR);
        v.textColor = p.getInt(KEY_TEXT_COLOR, DEFAULT_TEXT_COLOR);
        v.valueColor = p.getInt(KEY_VALUE_COLOR, DEFAULT_VALUE_COLOR);
        v.unitMode = p.getInt(KEY_UNIT_MODE, DEFAULT_UNIT_MODE) == Format.MODE_DAYS
                ? Format.MODE_DAYS : Format.MODE_ADAPTIVE;
        return v;
    }

    /**
     * 1.9 shipped a settings screen that reset the unit size and both spacings
     * to their minimums every time it opened, and wrote that back. A wiped
     * value is indistinguishable from a chosen one, so put those three back to
     * their defaults once. Colours, text size and the reading mode were never
     * touched by that bug and are left alone.
     */
    private static void repair(SharedPreferences p) {
        p.edit()
                .remove(KEY_UNIT_SP)
                .remove(KEY_PADDING_V_DP)
                .remove(KEY_PADDING_H_DP)
                .putInt(KEY_SCHEMA, SCHEMA)
                .apply();
    }

    public static void save(Context ctx, Values v) {
        prefs(ctx).edit()
                .putInt(KEY_TEXT_SP, clamp(v.textSp, MIN_TEXT_SP, MAX_TEXT_SP))
                .putInt(KEY_UNIT_SP, clamp(v.unitSp, MIN_UNIT_SP, MAX_UNIT_SP))
                .putInt(KEY_PADDING_V_DP,
                        clamp(v.paddingVerticalDp, MIN_PADDING_DP, MAX_PADDING_DP))
                .putInt(KEY_PADDING_H_DP,
                        clamp(v.paddingHorizontalDp, MIN_PADDING_DP, MAX_PADDING_DP))
                .putInt(KEY_BG_COLOR, v.bgColor)
                .putInt(KEY_TEXT_COLOR, v.textColor)
                .putInt(KEY_VALUE_COLOR, v.valueColor)
                .putInt(KEY_UNIT_MODE,
                        v.unitMode == Format.MODE_DAYS ? Format.MODE_DAYS : Format.MODE_ADAPTIVE)
                .putInt(KEY_SCHEMA, SCHEMA)
                .apply();
        Store.notifyWidgets(ctx);
    }

    /** Only the reading mode, for the places that need nothing else. */
    public static int unitMode(Context ctx) {
        return load(ctx).unitMode;
    }

    public static int withAlpha(int color, int alpha) {
        return (clamp(alpha, 0, 255) << 24) | (color & 0x00FFFFFF);
    }

    /** An ImageView tint needs opaque RGB; the alpha rides separately. */
    public static int opaque(int color) {
        return 0xFF000000 | (color & 0x00FFFFFF);
    }

    public static int dpToPx(Context ctx, float dp) {
        return (int) (dp * ctx.getResources().getDisplayMetrics().density + 0.5f);
    }

    private static int clamp(int value, int min, int max) {
        return value < min ? min : (value > max ? max : value);
    }
}
