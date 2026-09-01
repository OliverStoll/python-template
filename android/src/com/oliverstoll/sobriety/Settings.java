package com.oliverstoll.sobriety;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

/**
 * Everything about how the widget draws itself.
 *
 * <p>All of it is applied at runtime through {@code RemoteViews} — text sizes
 * and padding via {@code setTextViewTextSize} / {@code setViewPadding}, colours
 * via {@code setTextColor} and a tinted background image — rather than being
 * baked into the layout XML. Saving therefore redraws placed widgets with no
 * reinstall. The layout XML carries the same defaults, which keeps the first
 * frame correct before the adapter runs.
 */
public final class Settings {

    /** Base text size for a widget row's name and count, in sp. */
    public static final int DEFAULT_TEXT_SP = 13;
    public static final int MIN_TEXT_SP = 8;
    public static final int MAX_TEXT_SP = 22;

    /** Vertical padding inside a widget row, in dp. */
    public static final int DEFAULT_ROW_PADDING_DP = 3;
    public static final int MIN_ROW_PADDING_DP = 0;
    public static final int MAX_ROW_PADDING_DP = 14;

    public static final int DEFAULT_BG_COLOR = 0xE610151C;
    public static final int DEFAULT_TEXT_COLOR = 0xFFF2F5F8;
    public static final int DEFAULT_VALUE_COLOR = 0xFFF2F5F8;

    public static final int DEFAULT_UNIT_MODE = Format.MODE_ADAPTIVE;

    private static final String KEY_TEXT_SP = "widget_text_sp";
    private static final String KEY_ROW_PADDING_DP = "widget_row_padding_dp";
    private static final String KEY_BG_COLOR = "widget_bg_color";
    private static final String KEY_TEXT_COLOR = "widget_text_color";
    private static final String KEY_VALUE_COLOR = "widget_value_color";
    private static final String KEY_UNIT_MODE = "widget_unit_mode";

    private Settings() {}

    private static SharedPreferences prefs(Context ctx) {
        return Store.prefs(ctx);
    }

    public static int textSp(Context ctx) {
        return clamp(prefs(ctx).getInt(KEY_TEXT_SP, DEFAULT_TEXT_SP), MIN_TEXT_SP, MAX_TEXT_SP);
    }

    public static int rowPaddingDp(Context ctx) {
        return clamp(prefs(ctx).getInt(KEY_ROW_PADDING_DP, DEFAULT_ROW_PADDING_DP),
                MIN_ROW_PADDING_DP, MAX_ROW_PADDING_DP);
    }

    public static int bgColor(Context ctx) {
        return prefs(ctx).getInt(KEY_BG_COLOR, DEFAULT_BG_COLOR);
    }

    public static int textColor(Context ctx) {
        return prefs(ctx).getInt(KEY_TEXT_COLOR, DEFAULT_TEXT_COLOR);
    }

    public static int valueColor(Context ctx) {
        return prefs(ctx).getInt(KEY_VALUE_COLOR, DEFAULT_VALUE_COLOR);
    }

    public static int unitMode(Context ctx) {
        int mode = prefs(ctx).getInt(KEY_UNIT_MODE, DEFAULT_UNIT_MODE);
        return mode == Format.MODE_DAYS ? Format.MODE_DAYS : Format.MODE_ADAPTIVE;
    }

    public static void save(Context ctx, int textSp, int rowPaddingDp,
                            int bgColor, int textColor, int valueColor, int unitMode) {
        prefs(ctx).edit()
                .putInt(KEY_TEXT_SP, clamp(textSp, MIN_TEXT_SP, MAX_TEXT_SP))
                .putInt(KEY_ROW_PADDING_DP,
                        clamp(rowPaddingDp, MIN_ROW_PADDING_DP, MAX_ROW_PADDING_DP))
                .putInt(KEY_BG_COLOR, bgColor)
                .putInt(KEY_TEXT_COLOR, textColor)
                .putInt(KEY_VALUE_COLOR, valueColor)
                .putInt(KEY_UNIT_MODE, unitMode == Format.MODE_DAYS
                        ? Format.MODE_DAYS : Format.MODE_ADAPTIVE)
                .apply();
        Store.notifyWidgets(ctx);
    }

    public static void reset(Context ctx) {
        save(ctx, DEFAULT_TEXT_SP, DEFAULT_ROW_PADDING_DP, DEFAULT_BG_COLOR,
                DEFAULT_TEXT_COLOR, DEFAULT_VALUE_COLOR, DEFAULT_UNIT_MODE);
    }

    /** The emoji reads small next to the text, so give it a couple of points. */
    public static float iconSp(int textSp) {
        return textSp + 2f;
    }

    /** The unit word sits a shade below the number it follows. */
    public static float unitSp(int textSp) {
        return Math.max(MIN_TEXT_SP - 2f, textSp - 2f);
    }

    /** Horizontal row padding tracks the vertical setting, one step wider. */
    public static int rowPaddingHorizontalDp(int rowPaddingDp) {
        return rowPaddingDp + 1;
    }

    /** The widget's outer margin, so one slider tightens the whole thing. */
    public static int outerPaddingDp(int rowPaddingDp) {
        return rowPaddingDp + 3;
    }

    /** The unit word is the same colour as the name, just softer. */
    public static int unitColor(int textColor) {
        return withAlpha(textColor, Math.round(Color.alpha(textColor) * 0.65f));
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
