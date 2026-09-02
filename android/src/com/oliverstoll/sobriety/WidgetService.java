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
        private Settings.Values look = new Settings.Values();
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
            look = Settings.load(ctx);
            padVerticalPx = Settings.dpToPx(ctx, look.paddingVerticalDp);
            padHorizontalPx = Settings.dpToPx(ctx, look.paddingHorizontalDp);

            // Size both columns to the widest row, so every number starts at
            // the same x and every unit does too.
            List<String> values = new ArrayList<String>();
            List<String> units = new ArrayList<String>();
            for (Tracker t : items) {
                long elapsed = t.elapsed();
                values.add(Format.value(elapsed, look.unitMode));
                units.add(Format.unit(elapsed, look.unitMode));
            }
            valueWidthPx = Columns.width(ctx, values, look.textSp, true);
            unitWidthPx = Columns.width(ctx, units, look.unitSp, false);
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
            row.setTextViewText(R.id.w_days, Format.value(elapsed, look.unitMode));
            row.setTextViewText(R.id.w_unit, Format.unit(elapsed, look.unitMode));

            row.setViewPadding(R.id.widget_item_root,
                    padHorizontalPx, padVerticalPx, padHorizontalPx, padVerticalPx);
            row.setTextViewTextSize(R.id.w_icon, TypedValue.COMPLEX_UNIT_SP, look.iconSp());
            row.setTextViewTextSize(R.id.w_name, TypedValue.COMPLEX_UNIT_SP, look.textSp);
            row.setTextViewTextSize(R.id.w_days, TypedValue.COMPLEX_UNIT_SP, look.textSp);
            row.setTextViewTextSize(R.id.w_unit, TypedValue.COMPLEX_UNIT_SP, look.unitSp);

            row.setInt(R.id.w_days, "setMinWidth", valueWidthPx);
            row.setInt(R.id.w_unit, "setMinWidth", unitWidthPx);

            row.setTextColor(R.id.w_name, look.textColor);
            row.setTextColor(R.id.w_days, look.valueColor);
            row.setTextColor(R.id.w_unit, look.unitColor());

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
