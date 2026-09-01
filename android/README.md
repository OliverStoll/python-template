# Sober — a minimal sobriety tracker with a home-screen widget

Track any number of things you've quit. Each counter is an icon, a name, and a
start date; the app and the widget both show the number of days since.

<pre>
┌──────────────────────────┐
│ 🍺  Alcohol        128 d │
│ 🚬  Nicotine        41 d │
│ 🎰  Gambling       310 d │
└──────────────────────────┘
</pre>

## What's here

| Path | |
| --- | --- |
| `src/…/MainActivity.java` | The list, plus the add/edit/delete dialog |
| `src/…/SobrietyWidget.java` | `AppWidgetProvider` for the home-screen widget |
| `src/…/WidgetService.java` | `RemoteViewsFactory` backing the widget's list |
| `src/…/Store.java` | Persistence — one JSON blob in `SharedPreferences` |
| `src/…/Settings.java` | Every widget appearance value |
| `src/…/SettingsActivity.java` | The settings screen and its live preview |
| `src/…/ColorPicker.java` | Swatches, opacity slider, hex field |
| `src/…/Format.java` | Day counts to "4 months" / "128 days" |
| `src/…/Days.java` | Calendar-day arithmetic that survives DST |
| `build.sh` | Builds `out/Sober.apk` |
| `verify.sh` | Checks the APK's signature, alignment and manifest |
| `test.sh` | Plain-JVM tests for the formatting and colour logic |

No Gradle, no AndroidX, no third-party runtime dependencies — just the platform
framework, so the whole APK is 28 KB.

## Widget appearance

The gear in the app's header opens the settings screen — everything below is
edited over a live preview of a real widget row.

| | |
| --- | --- |
| Text size | 8–22 sp, default 13 |
| Row spacing | 0–14 dp, default 3 |
| Background colour | any colour, with an opacity slider |
| Name text colour | any colour |
| Number colour | any colour |
| Number format | adaptive, or always days |

Colours come from a 16-swatch grid, an opacity slider, or a `#RRGGBB` /
`#AARRGGBB` hex field. The unit word ("days", "weeks") is drawn at 65% of the
name colour's alpha, so it recedes without needing a fourth setting. Row
spacing also drives the widget's outer padding and the rows' horizontal
padding, so a single slider tightens the whole thing.

**Number format.** *Adaptive* picks the largest unit that still reads honestly:

| Days | Shown |
| --- | --- |
| 0–6 | `5 days` |
| 7–55 | `3 weeks` |
| 56–364 | `4 months` |
| 365+ | `2 years` |

*Always days* keeps the raw count — `128 days` — however large it gets. Either
way the number and the unit are separate views, so they get real spacing
between them and can be coloured apart.

All of it is applied at runtime — `setTextViewTextSize`, `setViewPadding`,
`setTextColor`, and a tinted background image — rather than being baked into
the layout, so saving redraws placed widgets immediately. No reinstall, no
re-adding the widget. The layout XML carries the same values as defaults, which
keeps the first frame correct before the adapter runs.

The background is an `ImageView` holding a white rounded-rectangle shape,
tinted with `setColorFilter` and faded with `setImageAlpha`. A plain
`setBackgroundColor` would have been simpler but would have squared off the
corners.

## Install

Copy `out/Sober.apk` to the phone and open it, or:

```
adb install -r out/Sober.apk
```

Then long-press the home screen → **Widgets** → **Sober** → *Sobriety counters*.
The widget is resizable; tapping any row opens the app.

Requires Android 7.0 (API 24) or newer.

## Build

```
./build.sh      # fetches its own toolchain on first run, writes out/Sober.apk
./verify.sh     # signature + alignment + manifest check
./test.sh       # formatting and colour-parsing tests, no device needed
```

`build.sh` needs only a JDK (17+), `python3`, `curl`, `unzip` and `zip`. It does
**not** need Android Studio or an installed Android SDK — on first run it
downloads the four pieces it actually needs into `~/atools`:

- `android.jar` (API 34 compile stubs) — GitHub
- `aapt2` (resource compiler/linker) — extracted from the `apktool-lib` jar on Maven Central
- `dalvik-dx` (class → dex) — Maven Central
- `apksig` (APK signing) — Maven Central

This roundabout sourcing exists because `dl.google.com`, the normal home of the
Android SDK, was unreachable from the build environment.

### Signing

`build.sh` generates a throwaway self-signed keystore at `debug.p12` on first
run (password `android`). It's git-ignored, so **keep it** if you want later
builds to install as upgrades — a fresh key means a different signature, and
Android will refuse to update the installed app until you uninstall it.

The APK is signed with **APK Signature Scheme v2 only**. That's sufficient here
because `minSdkVersion` is 24 and every Android 7.0+ device verifies v2. (v1 jar
signing is skipped deliberately: the last `apksig` release published to Maven
Central is 2.3.0, whose v1 path calls into `sun.security.pkcs` internals that
JDK 9 sealed off.)

## Notes

- Day counts roll over at local midnight. The widget refreshes hourly, on
  `ACTION_DATE_CHANGED`, and whenever you edit a counter.
- Data lives only on the device, in `SharedPreferences`. There is no network
  permission in the manifest.
