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
        // Format uses the device locale for the decimal separator; pin it so
        // the expected strings do not depend on where the tests run.
        java.util.Locale.setDefault(java.util.Locale.US);
        formatting();
        colours();
        history();
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
        eq(Format.full(hour, adaptive), "60 minutes", "an hour is still minutes");

        // A tenth of a day is where one decimal starts meaning anything.
        eq(Format.full(Format.TENTH_OF_DAY - 1, adaptive), "143 minutes", "last minute reading");
        eq(Format.full(Format.TENTH_OF_DAY, adaptive), "0.1 days", "first fractional reading");
        eq(Format.full(6 * hour, adaptive), "0.2 days", "quarter of a day");
        eq(Format.full(12 * hour, adaptive), "0.5 days", "half a day");
        eq(Format.full(day - 1, adaptive), "0.9 days", "floored, never reads 1.0 early");
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

        // Days mode differs only past the first day.
        eq(Format.full(42 * minute, daysOnly), "42 minutes", "days mode, minutes");
        eq(Format.full(6 * hour, daysOnly), "0.2 days", "days mode, fraction");
        eq(Format.full(day, daysOnly), "1 day", "days mode, one day");
        eq(Format.full(128 * day, daysOnly), "128 days", "days mode stays in days");
        eq(Format.full(730 * day, daysOnly), "730 days", "days mode never converts");

        eq(Format.value(128 * day, adaptive), "4", "value half");
        eq(Format.unit(128 * day, adaptive), "months", "unit half");
        eq(Format.unit(6 * hour, adaptive), "days", "a fraction is always plural");

        System.out.println("-- Refresh scheduling --");
        eq(Format.millisUntilChange(0, adaptive), minute, "wake in a minute at the start");
        eq(Format.millisUntilChange(90 * 1000L, adaptive), 30 * 1000L, "mid-minute remainder");
        eq(Format.millisUntilChange(Format.TENTH_OF_DAY, adaptive), Format.TENTH_OF_DAY,
                "a tenth of a day once fractional");
        eq(Format.millisUntilChange(Format.TENTH_OF_DAY + hour, adaptive),
                Format.TENTH_OF_DAY - hour, "mid-tenth remainder");
        eq(Format.millisUntilChange(day, adaptive), day, "daily once past a day");
        eq(Format.millisUntilChange(day + 6 * hour, adaptive), 18 * hour, "mid-day remainder");
        eq(Format.millisUntilChange(-5000L, adaptive), 5000L, "counts down to a future start");
    }

    private static void colours() {
        System.out.println("-- Colour parsing --");
        eq(hex(ColorPicker.parseHex("#FF4ADE80", true, 255)), hex(0xFF4ADE80), "full argb");
        eq(hex(ColorPicker.parseHex("FF4ADE80", true, 255)), hex(0xFF4ADE80), "leading hash optional");
        eq(hex(ColorPicker.parseHex("  #E610151C ", true, 255)), hex(0xE610151C), "surrounding space");
        eq(hex(ColorPicker.parseHex("#4ADE80", true, 0x80)), hex(0x804ADE80), "rgb keeps current alpha");
        eq(hex(ColorPicker.parseHex("#4ADE80", false, 0x80)), hex(0xFF4ADE80), "rgb forced opaque");
        eq(hex(ColorPicker.parseHex("#804ADE80", false, 255)), hex(0xFF4ADE80), "argb forced opaque");
        eq(ColorPicker.parseHex("#4ADE8", true, 255), null, "too short is rejected");
        eq(ColorPicker.parseHex("#", true, 255), null, "bare hash is rejected");
        eq(ColorPicker.parseHex("", true, 255), null, "empty is rejected");
        eq(ColorPicker.parseHex("#ZZZZZZ", true, 255), null, "non-hex is rejected");

        eq(ColorPicker.toHex(0xE610151C, true), "#E610151C", "toHex keeps alpha");
        eq(ColorPicker.toHex(0xE610151C, false), "#10151C", "toHex drops alpha");

        eq(hex(Settings.withAlpha(0xFF4ADE80, 0x80)), hex(0x804ADE80), "withAlpha");
        eq(hex(Settings.withAlpha(0xFF4ADE80, 999)), hex(0xFF4ADE80), "withAlpha clamps high");
        eq(hex(Settings.withAlpha(0xFF4ADE80, -5)), hex(0x004ADE80), "withAlpha clamps low");
        eq(hex(Settings.opaque(0x004ADE80)), hex(0xFF4ADE80), "opaque");
    }

    private static void history() {
        System.out.println("-- Relapse history --");
        long day = Format.DAY;
        long now = System.currentTimeMillis();
        long begin = now - 100 * day;

        Tracker t = new Tracker("id1", "🍺", "Alcohol", begin);
        eq(t.currentStart(), begin, "no slips means the original start");
        eq(t.relapses.size(), 0, "starts with an empty history");

        long firstSlip = now - 40 * day;
        long secondSlip = now - 10 * day;
        // Recorded out of order on purpose: the list must still come back sorted.
        t.recordRelapse(secondSlip);
        t.recordRelapse(firstSlip);
        eq(t.relapses.get(0), firstSlip, "history is kept ascending");
        eq(t.currentStart(), secondSlip, "the streak runs from the latest slip");
        eq(t.relapsesNewestFirst().get(0), secondSlip, "newest first for display");

        // Undoing a slip is the whole reason the history is kept.
        t.relapses.remove(Long.valueOf(secondSlip));
        eq(t.currentStart(), firstSlip, "removing a slip restores the earlier streak");
        t.relapses.remove(Long.valueOf(firstSlip));
        eq(t.currentStart(), begin, "removing every slip restores the original start");

        System.out.println("-- Storage round-trip --");
        Tracker saved = new Tracker("id2", "🚬", "Nicotine", begin);
        saved.recordRelapse(firstSlip);
        saved.recordRelapse(secondSlip);
        Tracker loaded = roundTrip(saved);
        eq(loaded.id, "id2", "id survives");
        eq(loaded.icon, "🚬", "icon survives");
        eq(loaded.name, "Nicotine", "name survives");
        eq(loaded.startMillis, begin, "start survives");
        eq(loaded.relapses.size(), 2, "both slips survive");
        eq(loaded.currentStart(), secondSlip, "streak start survives");

        // What a counter saved by v1.5 or earlier looks like.
        Tracker legacy = Tracker.fromJson(json(
                "{\"id\":\"old\",\"icon\":\"☕\",\"name\":\"Coffee\",\"start\":" + begin + "}"));
        eq(legacy.relapses.size(), 0, "a record with no relapses field loads");
        eq(legacy.currentStart(), begin, "and counts from its original start");

        Tracker empty = Tracker.fromJson(json("{}"));
        eq(empty.relapses.size(), 0, "an empty record does not throw");
    }

    /** Colours read as hex; everything else reads as itself. */
    private static String hex(Integer color) {
        return color == null ? "null" : String.format("#%08X", color);
    }

    private static Tracker roundTrip(Tracker t) {
        try {
            return Tracker.fromJson(new org.json.JSONObject(t.toJson().toString()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static org.json.JSONObject json(String raw) {
        try {
            return new org.json.JSONObject(raw);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
        if (value instanceof Long) return value + "ms";
        return value.toString();
    }
}
