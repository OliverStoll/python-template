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

        eq(Format.full(-1, adaptive), "—", "start date in the future");
        eq(Format.full(0, adaptive), "0 days", "day zero");
        eq(Format.full(1, adaptive), "1 day", "one day is singular");
        eq(Format.full(6, adaptive), "6 days", "last day before weeks");
        eq(Format.full(7, adaptive), "1 week", "first week is singular");
        eq(Format.full(13, adaptive), "1 week", "13 days rounds down");
        eq(Format.full(14, adaptive), "2 weeks", "two weeks");
        eq(Format.full(55, adaptive), "7 weeks", "last week before months");
        eq(Format.full(56, adaptive), "1 month", "first month is singular");
        eq(Format.full(90, adaptive), "3 months", "three months");
        eq(Format.full(364, adaptive), "12 months", "last month before years");
        eq(Format.full(365, adaptive), "1 year", "first year is singular");
        eq(Format.full(730, adaptive), "2 years", "two years");

        eq(Format.full(128, daysOnly), "128 days", "days mode stays in days");
        eq(Format.full(1, daysOnly), "1 day", "days mode is singular at one");
        eq(Format.full(0, daysOnly), "0 days", "days mode at zero");

        eq(Format.value(128, adaptive), "4", "value half");
        eq(Format.unit(128, adaptive), "months", "unit half");
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
        return value.toString();
    }
}
