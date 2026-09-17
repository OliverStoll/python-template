package com.oliverstoll.sobriety;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.TypedValue;

import java.util.List;

/**
 * Column widths for the widget's number and unit, measured from the text that
 * will actually be drawn.
 *
 * <p>A fixed dp would break as soon as the text size setting moved, and letting
 * the two columns size themselves left every row's number starting at a
 * different x. Measuring the widest value and the widest unit once per refresh
 * and pinning both columns to that keeps the rows aligned and still tight when
 * every counter is short.
 *
 * <p>The widths are pushed with {@code setMinWidth}, which RemoteViews has been
 * able to call since API 24. {@code setGravity} only became remotable later, so
 * the alignment within each column is set in the layout XML instead.
 */
public final class Columns {

    /** A little slack for the host process drawing with a slightly different font. */
    private static final float SAFETY_DP = 1f;

    private Columns() {}

    public static int width(Context ctx, List<String> texts, float sp, boolean bold) {
        if (texts.isEmpty()) return 0;

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp,
                ctx.getResources().getDisplayMetrics()));
        paint.setTypeface(bold ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);

        float widest = 0f;
        for (String text : texts) {
            if (text == null) continue;
            float w = paint.measureText(text);
            if (w > widest) widest = w;
        }
        return (int) Math.ceil(widest + Settings.dpToPx(ctx, SAFETY_DP));
    }
}
