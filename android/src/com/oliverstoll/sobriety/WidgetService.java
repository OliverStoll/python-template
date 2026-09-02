package com.oliverstoll.sobriety;

import android.content.Context;
import android.content.Intent;
import android.util.TypedValue;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.ArrayList;
import java.util.List;

public class WidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new Factory(getApplicationContext());
    }

    private static class Factory implements RemoteViewsService.RemoteViewsFactory {

        private final Context ctx;
        private List<Tracker> items = new ArrayList<Tracker>();

        // Snapshotted in onDataSetChanged so every row of one pass agrees.
        private int textSp = Settings.DEFAULT_TEXT_SP;
        private int textColor = Settings.DEFAULT_TEXT_COLOR;
        private int valueColor = Settings.DEFAULT_VALUE_COLOR;
        private int unitColor = Settings.DEFAULT_TEXT_COLOR;
        private int unitMode = Settings.DEFAULT_UNIT_MODE;
        private int padVerticalPx;
        private int padHorizontalPx;
        private int valueWidthPx;
        private int unitWidthPx;

        Factory(Context ctx) {
            this.ctx = ctx;
        }

        @Override public void onCreate() {}

        @Override public void onDataSetChanged() {
            items = Store.load(ctx);
            textSp = Settings.textSp(ctx);
            textColor = Settings.textColor(ctx);
            valueColor = Settings.valueColor(ctx);
            unitColor = Settings.unitColor(textColor);
            unitMode = Settings.unitMode(ctx);
            int padDp = Settings.rowPaddingDp(ctx);
            padVerticalPx = Settings.dpToPx(ctx, padDp);
            padHorizontalPx = Settings.dpToPx(ctx, Settings.rowPaddingHorizontalDp(padDp));

            // Size both columns to the widest row, so every number starts at
            // the same x and every unit does too.
            List<String> values = new ArrayList<String>();
            List<String> units = new ArrayList<String>();
            for (Tracker t : items) {
                long elapsed = t.elapsed();
                values.add(Format.value(elapsed, unitMode));
                units.add(Format.unit(elapsed, unitMode));
            }
            valueWidthPx = Columns.width(ctx, values, textSp, true);
            unitWidthPx = Columns.width(ctx, units, Settings.unitSp(textSp), false);
        }

        @Override public void onDestroy() {
            items.clear();
        }

        @Override public int getCount() {
            return items.size();
        }

        @Override public RemoteViews getViewAt(int position) {
            RemoteViews row = new RemoteViews(ctx.getPackageName(), R.layout.widget_item);
            if (position < 0 || position >= items.size()) return row;
            Tracker t = items.get(position);
            long elapsed = t.elapsed();

            row.setTextViewText(R.id.w_icon, t.icon);
            row.setTextViewText(R.id.w_name, t.name);
            row.setTextViewText(R.id.w_days, Format.value(elapsed, unitMode));
            row.setTextViewText(R.id.w_unit, Format.unit(elapsed, unitMode));

            row.setViewPadding(R.id.widget_item_root,
                    padHorizontalPx, padVerticalPx, padHorizontalPx, padVerticalPx);
            row.setTextViewTextSize(R.id.w_icon, TypedValue.COMPLEX_UNIT_SP,
                    Settings.iconSp(textSp));
            row.setTextViewTextSize(R.id.w_name, TypedValue.COMPLEX_UNIT_SP, textSp);
            row.setTextViewTextSize(R.id.w_days, TypedValue.COMPLEX_UNIT_SP, textSp);
            row.setTextViewTextSize(R.id.w_unit, TypedValue.COMPLEX_UNIT_SP,
                    Settings.unitSp(textSp));

            row.setInt(R.id.w_days, "setMinWidth", valueWidthPx);
            row.setInt(R.id.w_unit, "setMinWidth", unitWidthPx);

            row.setTextColor(R.id.w_name, textColor);
            row.setTextColor(R.id.w_days, valueColor);
            row.setTextColor(R.id.w_unit, unitColor);

            row.setOnClickFillInIntent(R.id.widget_item_root, new Intent());
            // The icon is the reset button; it sits inside the row, so it wins
            // the tap and the rest of the row still opens the app.
            Intent reset = new Intent()
                    .putExtra(WidgetTapActivity.EXTRA_ACTION, WidgetTapActivity.ACTION_RESET)
                    .putExtra(WidgetTapActivity.EXTRA_TRACKER_ID, t.id);
            row.setOnClickFillInIntent(R.id.w_icon, reset);
            return row;
        }

        @Override public RemoteViews getLoadingView() {
            return null;
        }

        @Override public int getViewTypeCount() {
            return 1;
        }

        @Override public long getItemId(int position) {
            return position;
        }

        @Override public boolean hasStableIds() {
            return false;
        }
    }
}
