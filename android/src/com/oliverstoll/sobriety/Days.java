package com.oliverstoll.sobriety;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/** Date and time helpers for the start-of-sobriety timestamp. */
public final class Days {

    private Days() {}

    /** Replaces the date of {@code millis}, keeping its time of day. */
    public static long withDate(long millis, int year, int month, int day) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(millis);
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, day);
        return c.getTimeInMillis();
    }

    /** Replaces the time of day of {@code millis}, keeping its date. */
    public static long withTime(long millis, int hour, int minute) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(millis);
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    public static int field(long millis, int calendarField) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(millis);
        return c.get(calendarField);
    }

    public static String formatDate(long millis) {
        return new SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(new Date(millis));
    }

    public static String formatTime(long millis) {
        return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(millis));
    }

    public static String formatDateTime(long millis) {
        return formatDate(millis) + ", " + formatTime(millis);
    }

    /** The subtitle under a counter's name: when the current streak started. */
    public static String startedAt(long millis) {
        return "since " + formatDateTime(millis);
    }
}
