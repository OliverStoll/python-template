package com.oliverstoll.sobriety;

/**
 * Plain-JVM checks for the parts that are pure logic — number formatting and
 * colour parsing. They run against the android.jar stubs on the classpath,
 * which is enough because none of the code under test calls into the framework.
 * Run with test.sh.
 */
public class Tests {

    private static int failures = 0;

    public static void main(String[] args) {
        formatting();
        colours();
        System.out.println(failures == 0
                ? "\nall passed"
                : "\n" + failures + " FAILED");
        System.exit(failures == 0 ? 0 : 1);
    }

    private static void formatting() {
        System.out.println("-- Format --");
        int adaptive = Format.MODE_ADAPTIVE;
        int daysOnly = Format.MODE_DAYS;
        long minute = Format.MINUTE;
        long hour = Format.HOUR;
        long day = Format.DAY;

        eq(Format.full(-1000L, adaptive), "—", "start in the future");

        // The first day is the whole point of tracking elapsed time.
        eq(Format.full(0, adaptive), "0 minutes", "the moment you start");
        eq(Format.full(59 * 1000L, adaptive), "0 minutes", "under a minute");
        eq(Format.full(minute, adaptive), "1 minute", "one minute is singular");
        eq(Format.full(42 * minute, adaptive), "42 minutes", "42 minutes");
        eq(Format.full(hour - 1, adaptive), "59 minutes", "last minute before hours");
        eq(Format.full(hour, adaptive), "1 hour", "one hour is singular");
        eq(Format.full(5 * hour, adaptive), "5 hours", "five hours");
        eq(Format.full(day - 1, adaptive), "23 hours", "last hour before days");
        eq(Format.full(day, adaptive), "1 day", "one day is singular");

        eq(Format.full(6 * day, adaptive), "6 days", "last day before weeks");
        eq(Format.full(7 * day, adaptive), "1 week", "first week is singular");
        eq(Format.full(13 * day, adaptive), "1 week", "13 days rounds down");
        eq(Format.full(14 * day, adaptive), "2 weeks", "two weeks");
        eq(Format.full(55 * day, adaptive), "7 weeks", "last week before months");
        eq(Format.full(56 * day, adaptive), "1 month", "first month is singular");
        eq(Format.full(90 * day, adaptive), "3 months", "three months");
        eq(Format.full(364 * day, adaptive), "12 months", "last month before years");
        eq(Format.full(365 * day, adaptive), "1 year", "first year is singular");
        eq(Format.full(730 * day, adaptive), "2 years", "two years");

        // Days mode still reads in minutes and hours before day one is up.
        eq(Format.full(42 * minute, daysOnly), "42 minutes", "days mode, minutes");
        eq(Format.full(5 * hour, daysOnly), "5 hours", "days mode, hours");
        eq(Format.full(day, daysOnly), "1 day", "days mode, one day");
        eq(Format.full(128 * day, daysOnly), "128 days", "days mode stays in days");
        eq(Format.full(730 * day, daysOnly), "730 days", "days mode never converts");

        eq(Format.value(128 * day, adaptive), "4", "value half");
        eq(Format.unit(128 * day, adaptive), "months", "unit half");

        System.out.println("-- Refresh scheduling --");
        eq(Format.millisUntilChange(0, adaptive), minute, "wake in a minute at the start");
        eq(Format.millisUntilChange(90 * 1000L, adaptive), 30 * 1000L, "mid-minute remainder");
        eq(Format.millisUntilChange(hour, adaptive), hour, "hourly once past an hour");
        eq(Format.millisUntilChange(hour + 15 * minute, adaptive), 45 * minute, "mid-hour remainder");
        eq(Format.millisUntilChange(day, adaptive), day, "daily once past a day");
        eq(Format.millisUntilChange(-5000L, adaptive), 5000L, "counts down to a future start");
    }

    private static void colours() {
        System.out.println("-- Colour parsing --");
        eq(ColorPicker.parseHex("#FF4ADE80", true, 255), 0xFF4ADE80, "full argb");
        eq(ColorPicker.parseHex("FF4ADE80", true, 255), 0xFF4ADE80, "leading hash optional");
        eq(ColorPicker.parseHex("  #E610151C ", true, 255), 0xE610151C, "surrounding space");
        eq(ColorPicker.parseHex("#4ADE80", true, 0x80), 0x804ADE80, "rgb keeps current alpha");
        eq(ColorPicker.parseHex("#4ADE80", false, 0x80), 0xFF4ADE80, "rgb forced opaque");
        eq(ColorPicker.parseHex("#804ADE80", false, 255), 0xFF4ADE80, "argb forced opaque");
        eq(ColorPicker.parseHex("#4ADE8", true, 255), null, "too short is rejected");
        eq(ColorPicker.parseHex("#", true, 255), null, "bare hash is rejected");
        eq(ColorPicker.parseHex("", true, 255), null, "empty is rejected");
        eq(ColorPicker.parseHex("#ZZZZZZ", true, 255), null, "non-hex is rejected");

        eq(ColorPicker.toHex(0xE610151C, true), "#E610151C", "toHex keeps alpha");
        eq(ColorPicker.toHex(0xE610151C, false), "#10151C", "toHex drops alpha");

        eq(Settings.withAlpha(0xFF4ADE80, 0x80), 0x804ADE80, "withAlpha");
        eq(Settings.withAlpha(0xFF4ADE80, 999), 0xFF4ADE80, "withAlpha clamps high");
        eq(Settings.withAlpha(0xFF4ADE80, -5), 0x004ADE80, "withAlpha clamps low");
        eq(Settings.opaque(0x004ADE80), 0xFF4ADE80, "opaque");
    }

    private static void eq(Object actual, Object expected, String what) {
        String got = render(actual);
        String want = render(expected);
        boolean ok = got.equals(want);
        if (!ok) failures++;
        System.out.printf("  %-34s %-12s %s%n", what, got, ok ? "ok" : "EXPECTED " + want);
    }

    private static String render(Object value) {
        if (value == null) return "null";
        if (value instanceof Integer) return String.format("#%08X", (Integer) value);
        if (value instanceof Long) return value + "ms";
        return value.toString();
    }
}
