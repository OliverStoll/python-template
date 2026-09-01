package com.oliverstoll.sobriety;

/**
 * Turns a day count into a number and a spelled-out unit, kept as two pieces so
 * the widget can colour and space them independently.
 */
public final class Format {

    /** Largest unit that still reads honestly: days, then weeks, months, years. */
    public static final int MODE_ADAPTIVE = 0;
    /** Always the raw day count, however large it gets. */
    public static final int MODE_DAYS = 1;

    /** Below one week, days are the only sensible unit. */
    private static final int WEEK = 7;
    /** Past eight weeks, "14 weeks" starts to lose its shape — switch to months. */
    private static final int WEEKS_LIMIT = 56;
    private static final int YEAR = 365;
    private static final int MONTH = 30;

    private Format() {}

    /** The bare number, e.g. "128" or "4". "—" for a start date in the future. */
    public static String value(int days, int mode) {
        if (days < 0) return "—";
        return String.valueOf(count(days, mode));
    }

    /** The unit word, already singular or plural: "day", "weeks", "months". */
    public static String unit(int days, int mode) {
        if (days < 0) return "";
        int n = count(days, mode);
        String unit = unitName(days, mode);
        return n == 1 ? unit : unit + "s";
    }

    /** The two halves joined, for places that render one string. */
    public static String full(int days, int mode) {
        if (days < 0) return "—";
        return value(days, mode) + " " + unit(days, mode);
    }

    private static int count(int days, int mode) {
        if (mode == MODE_DAYS || days < WEEK) return days;
        if (days < WEEKS_LIMIT) return days / WEEK;
        if (days < YEAR) return days / MONTH;
        return days / YEAR;
    }

    private static String unitName(int days, int mode) {
        if (mode == MODE_DAYS || days < WEEK) return "day";
        if (days < WEEKS_LIMIT) return "week";
        if (days < YEAR) return "month";
        return "year";
    }
}
