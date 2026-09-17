package com.oliverstoll.sobriety;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.GradientDrawable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * A colour picker: saturation/value square, hue strip, optional opacity strip,
 * quick swatches and a hex field — all kept in sync off one HSVA source of truth.
 */
public final class ColorPicker {

    public interface OnPicked {
        void onPicked(int color);
    }

    private static final int[] SWATCHES = {
            0xFF10151C, 0xFF1B2430, 0xFF000000, 0xFFFFFFFF,
            0xFF4ADE80, 0xFF60A5FA, 0xFFF472B6, 0xFFFBBF24,
    };

    private static final int[] HUES = {
            0xFFFF0000, 0xFFFFFF00, 0xFF00FF00, 0xFF00FFFF,
            0xFF0000FF, 0xFFFF00FF, 0xFFFF0000,
    };

    private ColorPicker() {}

    public static void show(final Activity activity, String title, int initial,
                            final boolean allowAlpha, final OnPicked callback) {

        // One source of truth; every control reads and writes only through this.
        final float[] hsv = new float[3];
        Color.colorToHSV(initial, hsv);
        final int[] alpha = { allowAlpha ? Color.alpha(initial) : 255 };

        int pad = Settings.dpToPx(activity, 20);
        LinearLayout root = new LinearLayout(activity);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad, Settings.dpToPx(activity, 8), pad, 0);

        final View preview = new View(activity);
        preview.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, Settings.dpToPx(activity, 44)));
        root.addView(preview);

        final SatValView satVal = new SatValView(activity);
        LinearLayout.LayoutParams svParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, Settings.dpToPx(activity, 170));
        svParams.topMargin = Settings.dpToPx(activity, 12);
        satVal.setLayoutParams(svParams);
        root.addView(satVal);

        final StripView hue = new StripView(activity, HUES, true);
        LinearLayout.LayoutParams stripParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, Settings.dpToPx(activity, 30));
        stripParams.topMargin = Settings.dpToPx(activity, 12);
        hue.setLayoutParams(stripParams);
        root.addView(hue);

        final StripView opacity = new StripView(activity, new int[] { 0x00000000, 0xFF000000 }, false);
        if (allowAlpha) {
            LinearLayout.LayoutParams alphaParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, Settings.dpToPx(activity, 30));
            alphaParams.topMargin = Settings.dpToPx(activity, 10);
            opacity.setLayoutParams(alphaParams);
            root.addView(opacity);
        }

        final EditText hex = new EditText(activity);
        hex.setSingleLine(true);
        hex.setFilters(new InputFilter[] { new InputFilter.LengthFilter(9) });
        hex.setTextSize(16);

        // Guards re-entry: every control writes hsv/alpha then calls sync(),
        // and sync() writes back to the controls.
        final boolean[] syncing = { false };
        final boolean[] editingHex = { false };

        final Runnable sync = new Runnable() {
            @Override
            public void run() {
                if (syncing[0]) return;
                syncing[0] = true;
                try {
                    int opaque = Color.HSVToColor(hsv);
                    int color = Settings.withAlpha(opaque, alpha[0]);

                    GradientDrawable shape = new GradientDrawable();
                    shape.setColor(color);
                    shape.setCornerRadius(Settings.dpToPx(activity, 10));
                    shape.setStroke(Settings.dpToPx(activity, 1), 0x44FFFFFF);
                    preview.setBackground(shape);

                    satVal.setHue(hsv[0]);
                    satVal.setPosition(hsv[1], hsv[2]);
                    hue.setFraction(hsv[0] / 360f);
                    if (allowAlpha) {
                        opacity.setColors(new int[] {
                                Settings.withAlpha(opaque, 0), opaque });
                        opacity.setFraction(alpha[0] / 255f);
                    }

                    // Rewriting the field mid-keystroke would move the caret.
                    if (!editingHex[0]) {
                        String want = toHex(color, allowAlpha);
                        if (!want.equalsIgnoreCase(hex.getText().toString().trim())) {
                            hex.setText(want);
                        }
                    }
                } finally {
                    syncing[0] = false;
                }
            }
        };

        satVal.setListener(new SatValView.OnChanged() {
            @Override
            public void onChanged(float saturation, float value) {
                hsv[1] = saturation;
                hsv[2] = value;
                sync.run();
            }
        });

        hue.setListener(new StripView.OnChanged() {
            @Override
            public void onChanged(float fraction) {
                hsv[0] = fraction * 360f;
                sync.run();
            }
        });

        opacity.setListener(new StripView.OnChanged() {
            @Override
            public void onChanged(float fraction) {
                alpha[0] = Math.round(fraction * 255f);
                sync.run();
            }
        });

        LinearLayout swatchRow = new LinearLayout(activity);
        swatchRow.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        rowParams.topMargin = Settings.dpToPx(activity, 12);
        swatchRow.setLayoutParams(rowParams);
        for (int i = 0; i < SWATCHES.length; i++) {
            final int swatchColor = SWATCHES[i];
            View cell = new View(activity);
            LinearLayout.LayoutParams cellParams = new LinearLayout.LayoutParams(
                    0, Settings.dpToPx(activity, 32), 1f);
            int gap = Settings.dpToPx(activity, 2);
            cellParams.setMargins(gap, 0, gap, 0);
            cell.setLayoutParams(cellParams);

            GradientDrawable shape = new GradientDrawable();
            shape.setColor(swatchColor);
            shape.setCornerRadius(Settings.dpToPx(activity, 6));
            shape.setStroke(Settings.dpToPx(activity, 1), 0x44FFFFFF);
            cell.setBackground(shape);

            cell.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Swatches set the hue and shade, never the chosen opacity.
                    Color.colorToHSV(swatchColor, hsv);
                    sync.run();
                }
            });
            swatchRow.addView(cell);
        }
        root.addView(swatchRow);

        TextView hexLabel = new TextView(activity);
        hexLabel.setText(allowAlpha ? "Hex (#AARRGGBB or #RRGGBB)" : "Hex (#RRGGBB)");
        hexLabel.setTextSize(12);
        hexLabel.setPadding(0, Settings.dpToPx(activity, 12), 0, 0);
        root.addView(hexLabel);

        hex.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (syncing[0]) return;
                Integer parsed = parseHex(s.toString(), allowAlpha, alpha[0]);
                if (parsed == null) return;
                Color.colorToHSV(parsed.intValue(), hsv);
                if (allowAlpha) alpha[0] = Color.alpha(parsed.intValue());
                editingHex[0] = true;
                try {
                    sync.run();
                } finally {
                    editingHex[0] = false;
                }
            }
        });
        root.addView(hex);

        sync.run();

        new AlertDialog.Builder(activity)
                .setTitle(title)
                .setView(root)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Select", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int which) {
                        callback.onPicked(
                                Settings.withAlpha(Color.HSVToColor(hsv), alpha[0]));
                    }
                })
                .show();
    }

    /** The saturation/value square for the current hue. */
    private static class SatValView extends View {

        interface OnChanged {
            void onChanged(float saturation, float value);
        }

        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint cursor = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF bounds = new RectF();
        private float hue;
        private float saturation = 1f;
        private float value = 1f;
        private OnChanged listener;

        SatValView(Context ctx) {
            super(ctx);
            cursor.setStyle(Paint.Style.STROKE);
            cursor.setStrokeWidth(Settings.dpToPx(ctx, 2));
        }

        void setListener(OnChanged listener) {
            this.listener = listener;
        }

        void setHue(float hue) {
            if (this.hue == hue) return;
            this.hue = hue;
            invalidate();
        }

        void setPosition(float saturation, float value) {
            this.saturation = saturation;
            this.value = value;
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            float w = getWidth();
            float h = getHeight();
            if (w <= 0 || h <= 0) return;
            bounds.set(0, 0, w, h);
            float radius = Settings.dpToPx(getContext(), 10);

            // Two passes rather than a ComposeShader: composing two gradients of
            // the same type is not reliable under hardware acceleration.
            int pure = Color.HSVToColor(new float[] { hue, 1f, 1f });
            paint.setShader(new LinearGradient(0, 0, w, 0, Color.WHITE, pure, Shader.TileMode.CLAMP));
            canvas.drawRoundRect(bounds, radius, radius, paint);
            paint.setShader(new LinearGradient(0, 0, 0, h, Color.TRANSPARENT, Color.BLACK,
                    Shader.TileMode.CLAMP));
            canvas.drawRoundRect(bounds, radius, radius, paint);
            paint.setShader(null);

            float cx = saturation * w;
            float cy = (1f - value) * h;
            float r = Settings.dpToPx(getContext(), 7);
            cursor.setColor(value > 0.55f && saturation < 0.55f ? 0xFF202020 : Color.WHITE);
            canvas.drawCircle(cx, cy, r, cursor);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            int action = event.getAction();
            if (action != MotionEvent.ACTION_DOWN && action != MotionEvent.ACTION_MOVE
                    && action != MotionEvent.ACTION_UP) {
                return super.onTouchEvent(event);
            }
            // The square is inside a scrolling dialog; keep the drag.
            if (getParent() != null) getParent().requestDisallowInterceptTouchEvent(true);
            float w = Math.max(1, getWidth());
            float h = Math.max(1, getHeight());
            saturation = clamp(event.getX() / w);
            value = 1f - clamp(event.getY() / h);
            invalidate();
            if (listener != null) listener.onChanged(saturation, value);
            return true;
        }
    }

    /** A horizontal gradient strip with a draggable thumb: hue, or opacity. */
    private static class StripView extends View {

        interface OnChanged {
            void onChanged(float fraction);
        }

        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint checker = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint thumb = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF bounds = new RectF();
        private final boolean evenlySpaced;
        private int[] colors;
        private float fraction;
        private OnChanged listener;

        StripView(Context ctx, int[] colors, boolean evenlySpaced) {
            super(ctx);
            this.colors = colors;
            this.evenlySpaced = evenlySpaced;
            thumb.setStyle(Paint.Style.STROKE);
            thumb.setStrokeWidth(Settings.dpToPx(ctx, 2));
            thumb.setColor(Color.WHITE);
            checker.setColor(0xFF808080);
        }

        void setListener(OnChanged listener) {
            this.listener = listener;
        }

        void setColors(int[] colors) {
            this.colors = colors;
            invalidate();
        }

        void setFraction(float fraction) {
            this.fraction = clamp(fraction);
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            float w = getWidth();
            float h = getHeight();
            if (w <= 0 || h <= 0) return;
            bounds.set(0, 0, w, h);
            float radius = h / 2f;

            // A grey underlay so partial opacity is visible against the dialog.
            if (!evenlySpaced) {
                canvas.drawRoundRect(bounds, radius, radius, checker);
            }
            paint.setShader(new LinearGradient(0, 0, w, 0, colors, null, Shader.TileMode.CLAMP));
            canvas.drawRoundRect(bounds, radius, radius, paint);
            paint.setShader(null);

            float cx = fraction * w;
            canvas.drawCircle(cx, h / 2f, h / 2f - Settings.dpToPx(getContext(), 3), thumb);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            int action = event.getAction();
            if (action != MotionEvent.ACTION_DOWN && action != MotionEvent.ACTION_MOVE
                    && action != MotionEvent.ACTION_UP) {
                return super.onTouchEvent(event);
            }
            if (getParent() != null) getParent().requestDisallowInterceptTouchEvent(true);
            fraction = clamp(event.getX() / Math.max(1, getWidth()));
            invalidate();
            if (listener != null) listener.onChanged(fraction);
            return true;
        }
    }

    private static float clamp(float value) {
        return value < 0f ? 0f : (value > 1f ? 1f : value);
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
