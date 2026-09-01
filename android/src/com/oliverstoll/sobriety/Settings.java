package com.oliverstoll.sobriety;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * How densely the widget draws itself.
 *
 * <p>Both values are applied at runtime through {@code RemoteViews}
 * ({@code setTextViewTextSize} / {@code setViewPadding}) rather than baked into
 * the layout XML, so changing them redraws placed widgets with no reinstall.
 * The XML values are the defaults below, which keeps the widget looking right
 * for the first frame before the adapter runs.
 */
public final class Settings {

    /** Base text size for a widget row's name and day count, in sp. */
    public static final int DEFAULT_TEXT_SP = 13;
    public static final int MIN_TEXT_SP = 8;
    public static final int MAX_TEXT_SP = 22;

    /** Vertical padding inside a widget row, in dp. */
    public static final int DEFAULT_ROW_PADDING_DP = 3;
    public static final int MIN_ROW_PADDING_DP = 0;
    public static final int MAX_ROW_PADDING_DP = 14;

    private static final String KEY_TEXT_SP = "widget_text_sp";
    private static final String KEY_ROW_PADDING_DP = "widget_row_padding_dp";

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

    public static void save(Context ctx, int textSp, int rowPaddingDp) {
        prefs(ctx).edit()
                .putInt(KEY_TEXT_SP, clamp(textSp, MIN_TEXT_SP, MAX_TEXT_SP))
                .putInt(KEY_ROW_PADDING_DP, clamp(rowPaddingDp, MIN_ROW_PADDING_DP, MAX_ROW_PADDING_DP))
                .apply();
        Store.notifyWidgets(ctx);
    }

    /** The emoji reads small next to the text, so give it a couple of points. */
    public static float iconSp(int textSp) {
        return textSp + 2f;
    }

    /** Horizontal row padding tracks the vertical setting, one step wider. */
    public static int rowPaddingHorizontalDp(int rowPaddingDp) {
        return rowPaddingDp + 1;
    }

    /** The widget's outer margin, so one slider tightens the whole thing. */
    public static int outerPaddingDp(int rowPaddingDp) {
        return rowPaddingDp + 3;
    }

    public static int dpToPx(Context ctx, float dp) {
        return (int) (dp * ctx.getResources().getDisplayMetrics().density + 0.5f);
    }

    private static int clamp(int value, int min, int max) {
        return value < min ? min : (value > max ? max : value);
    }
}
