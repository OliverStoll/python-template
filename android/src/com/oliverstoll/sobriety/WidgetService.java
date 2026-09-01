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
        private int textSp = Settings.DEFAULT_TEXT_SP;
        private int padVerticalPx;
        private int padHorizontalPx;

        Factory(Context ctx) {
            this.ctx = ctx;
        }

        @Override public void onCreate() {}

        @Override public void onDataSetChanged() {
            items = Store.load(ctx);
            textSp = Settings.textSp(ctx);
            int padDp = Settings.rowPaddingDp(ctx);
            padVerticalPx = Settings.dpToPx(ctx, padDp);
            padHorizontalPx = Settings.dpToPx(ctx, Settings.rowPaddingHorizontalDp(padDp));
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
            int days = t.days();
            row.setTextViewText(R.id.w_icon, t.icon);
            row.setTextViewText(R.id.w_name, t.name);
            row.setTextViewText(R.id.w_days, days < 0 ? "—" : String.valueOf(days) + "d");

            row.setViewPadding(R.id.widget_item_root,
                    padHorizontalPx, padVerticalPx, padHorizontalPx, padVerticalPx);
            row.setTextViewTextSize(R.id.w_icon, TypedValue.COMPLEX_UNIT_SP,
                    Settings.iconSp(textSp));
            row.setTextViewTextSize(R.id.w_name, TypedValue.COMPLEX_UNIT_SP, textSp);
            row.setTextViewTextSize(R.id.w_days, TypedValue.COMPLEX_UNIT_SP, textSp);

            row.setOnClickFillInIntent(R.id.widget_item_root, new Intent());
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
