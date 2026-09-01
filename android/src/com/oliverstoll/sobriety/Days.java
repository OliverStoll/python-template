package com.oliverstoll.sobriety;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/** Calendar-day arithmetic that survives daylight-saving shifts. */
public final class Days {

    private Days() {}

    public static long startOfDay(long millis) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(millis);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    public static int between(long fromMillis, long toMillis) {
        long a = startOfDay(fromMillis);
        long b = startOfDay(toMillis);
        // Round rather than truncate: DST makes some days 23 or 25 hours long.
        long days = Math.round((b - a) / 86400000.0);
        return (int) days;
    }

    public static String format(long millis) {
        return new SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(new Date(millis));
    }

    /** "3 weeks", "1 year, 2 months" — a friendlier restatement of the day count. */
    public static String humanSince(long startMillis) {
        int d = between(startMillis, System.currentTimeMillis());
        if (d < 0) return "starts " + format(startMillis);
        if (d == 0) return "since today";
        if (d < 7) return "since " + format(startMillis);
        if (d < 60) return plural(d / 7, "week") + " clean";
        if (d < 365) return plural(d / 30, "month") + " clean";
        int years = d / 365;
        int months = (d % 365) / 30;
        if (months == 0) return plural(years, "year") + " clean";
        return plural(years, "year") + ", " + plural(months, "month");
    }

    private static String plural(int n, String unit) {
        return n + " " + unit + (n == 1 ? "" : "s");
    }
}
