package com.oliverstoll.sobriety;

import java.util.Locale;

/**
 * Turns elapsed sobriety into a number and a spelled-out unit, kept as two
 * pieces so the widget can colour and space them independently.
 *
 * <p>Everything works on elapsed milliseconds rather than calendar days, which
 * is what makes the first day legible: a counter started at 3pm reads
 * "20 minutes", then "0.3 days", then "1 day" at 3pm the next day. A
 * calendar-day count would have shown "0 days" all evening and jumped at
 * midnight.
 */
public final class Format {

    /** Largest unit that still reads honestly, from minutes up to years. */
    public static final int MODE_ADAPTIVE = 0;
    /** Days once past the first day; minutes and fractions before that. */
    public static final int MODE_DAYS = 1;

    public static final long MINUTE = 60000L;
    public static final long HOUR = 60L * MINUTE;
    public static final long DAY = 24L * HOUR;

    /** The step of the fractional display: one decimal place of a day. */
    static final long TENTH_OF_DAY = DAY / 10;

    /**
     * Below this, minutes; above it, tenths of a day.
     *
     * <p>It is 2h24m rather than an hour because one decimal cannot express an
     * hour — 1/24 of a day rounds to "0.0 days". The handover has to sit where
     * the first tenth actually begins.
     */
    private static final long FRACTION_START = TENTH_OF_DAY;

    private static final long WEEK = 7L * DAY;
    /** Past eight weeks "14 weeks" loses its shape — switch to months. */
    private static final long WEEKS_LIMIT = 8L * WEEK;
    private static final long MONTH = 30L * DAY;
    private static final long YEAR = 365L * DAY;

    private static final int TIER_FUTURE = 0;
    private static final int TIER_MINUTES = 1;
    private static final int TIER_PART_DAY = 2;
    private static final int TIER_DAYS = 3;
    private static final int TIER_WEEKS = 4;
    private static final int TIER_MONTHS = 5;
    private static final int TIER_YEARS = 6;

    private Format() {}

    /** The bare number, e.g. "42", "0.3" or "128". "—" for a future start. */
    public static String value(long elapsed, int mode) {
        switch (tier(elapsed, mode)) {
            case TIER_FUTURE:
                return "—";
            case TIER_MINUTES:
                return String.valueOf(elapsed / MINUTE);
            case TIER_PART_DAY:
                // Floored, not rounded: 0.99 of a day must not read "1.0".
                return String.format(Locale.getDefault(), "%.1f",
                        (elapsed / TENTH_OF_DAY) / 10.0);
            case TIER_DAYS:
                return String.valueOf(elapsed / DAY);
            case TIER_WEEKS:
                return String.valueOf(elapsed / WEEK);
            case TIER_MONTHS:
                return String.valueOf(elapsed / MONTH);
            default:
                return String.valueOf(elapsed / YEAR);
        }
    }

    /** The unit word, already singular or plural: "minutes", "days", "year". */
    public static String unit(long elapsed, int mode) {
        int tier = tier(elapsed, mode);
        if (tier == TIER_FUTURE) return "";
        // A fraction of a day is always plural — it only ever runs 0.1 to 0.9.
        if (tier == TIER_PART_DAY) return "days";

        String name = unitName(tier);
        return whole(elapsed, tier) == 1 ? name : name + "s";
    }

    /** The two halves joined, for places that render one string. */
    public static String full(long elapsed, int mode) {
        if (elapsed < 0) return "—";
        return value(elapsed, mode) + " " + unit(elapsed, mode);
    }

    /**
     * How long until the displayed number would change, so the widget can be
     * woken exactly when it goes stale instead of polling.
     */
    public static long millisUntilChange(long elapsed, int mode) {
        if (elapsed < 0) return -elapsed;
        long step = step(tier(elapsed, mode));
        return step - (elapsed % step);
    }

    private static int tier(long elapsed, int mode) {
        if (elapsed < 0) return TIER_FUTURE;
        if (elapsed < FRACTION_START) return TIER_MINUTES;
        if (elapsed < DAY) return TIER_PART_DAY;
        if (mode == MODE_DAYS || elapsed < WEEK) return TIER_DAYS;
        if (elapsed < WEEKS_LIMIT) return TIER_WEEKS;
        if (elapsed < YEAR) return TIER_MONTHS;
        return TIER_YEARS;
    }

    private static long step(int tier) {
        switch (tier) {
            case TIER_MINUTES: return MINUTE;
            case TIER_PART_DAY: return TENTH_OF_DAY;
            default: return DAY;
        }
    }

    private static long whole(long elapsed, int tier) {
        switch (tier) {
            case TIER_MINUTES: return elapsed / MINUTE;
            case TIER_DAYS: return elapsed / DAY;
            case TIER_WEEKS: return elapsed / WEEK;
            case TIER_MONTHS: return elapsed / MONTH;
            default: return elapsed / YEAR;
        }
    }

    private static String unitName(int tier) {
        switch (tier) {
            case TIER_MINUTES: return "minute";
            case TIER_DAYS: return "day";
            case TIER_WEEKS: return "week";
            case TIER_MONTHS: return "month";
            default: return "year";
        }
    }
}
