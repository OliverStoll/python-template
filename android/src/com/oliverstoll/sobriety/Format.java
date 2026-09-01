package com.oliverstoll.sobriety;

/**
 * Turns elapsed sobriety into a number and a spelled-out unit, kept as two
 * pieces so the widget can colour and space them independently.
 *
 * <p>Everything here works on elapsed milliseconds rather than calendar days,
 * which is what makes the first hours legible: a counter started at 3pm reads
 * "20 minutes", then "6 hours", then "1 day" at 3pm the next day. A
 * calendar-day count would have shown "0 days" for the whole first evening and
 * then jumped at midnight.
 */
public final class Format {

    /** Largest unit that still reads honestly, from minutes up to years. */
    public static final int MODE_ADAPTIVE = 0;
    /** Days once past the first day; minutes and hours before that. */
    public static final int MODE_DAYS = 1;

    public static final long MINUTE = 60000L;
    public static final long HOUR = 60L * MINUTE;
    public static final long DAY = 24L * HOUR;

    private static final long WEEK = 7L * DAY;
    /** Past eight weeks "14 weeks" loses its shape — switch to months. */
    private static final long WEEKS_LIMIT = 8L * WEEK;
    private static final long MONTH = 30L * DAY;
    private static final long YEAR = 365L * DAY;

    private Format() {}

    /** The bare number, e.g. "42" or "6". "—" for a start still in the future. */
    public static String value(long elapsed, int mode) {
        if (elapsed < 0) return "—";
        return String.valueOf(count(elapsed, mode));
    }

    /** The unit word, already singular or plural: "minutes", "hour", "days". */
    public static String unit(long elapsed, int mode) {
        if (elapsed < 0) return "";
        return count(elapsed, mode) == 1
                ? unitName(elapsed, mode)
                : unitName(elapsed, mode) + "s";
    }

    /** The two halves joined, for places that render one string. */
    public static String full(long elapsed, int mode) {
        if (elapsed < 0) return "—";
        return value(elapsed, mode) + " " + unit(elapsed, mode);
    }

    private static long count(long elapsed, int mode) {
        if (elapsed < HOUR) return elapsed / MINUTE;
        if (elapsed < DAY) return elapsed / HOUR;
        if (mode == MODE_DAYS || elapsed < WEEK) return elapsed / DAY;
        if (elapsed < WEEKS_LIMIT) return elapsed / WEEK;
        if (elapsed < YEAR) return elapsed / MONTH;
        return elapsed / YEAR;
    }

    private static String unitName(long elapsed, int mode) {
        if (elapsed < HOUR) return "minute";
        if (elapsed < DAY) return "hour";
        if (mode == MODE_DAYS || elapsed < WEEK) return "day";
        if (elapsed < WEEKS_LIMIT) return "week";
        if (elapsed < YEAR) return "month";
        return "year";
    }

    /**
     * How long until the displayed number would change, so the widget can be
     * woken exactly when it goes stale instead of polling.
     */
    public static long millisUntilChange(long elapsed, int mode) {
        if (elapsed < 0) return -elapsed;
        long step = elapsed < HOUR ? MINUTE : (elapsed < DAY ? HOUR : DAY);
        long remainder = elapsed % step;
        return step - remainder;
    }
}
