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
| `src/…/Reset.java` | The confirmation prompt shared by both reset paths |
| `src/…/WidgetTapActivity.java` | Where widget taps land: reset, or open the app |
| `src/…/Settings.java` | Every widget appearance value |
| `src/…/SettingsActivity.java` | The settings screen and its live preview |
| `src/…/ColorPicker.java` | Swatches, opacity slider, hex field |
| `src/…/Format.java` | Elapsed time to "42 minutes" / "4 months" |
| `src/…/Columns.java` | Measures the widget's number and unit columns |
| `src/…/Days.java` | Date and time helpers for the start timestamp |
| `build.sh` | Builds `out/Sober.apk` |
| `verify.sh` | Checks the APK's signature, alignment and manifest |
| `test.sh` | Plain-JVM tests for the formatting and colour logic |

No Gradle, no AndroidX, no third-party runtime dependencies — just the platform
framework, so the whole APK is 28 KB.

## Resetting, and the history

Tap a counter's **icon** to reset it — in the app or in the widget, same
gesture in both. Either way a confirmation names the counter and says when the
current streak began; confirming records a slip at that moment and restarts the
count from there.

Resets are not destructive. `startMillis` never moves; each reset appends a
timestamp to the counter's `relapses` list, and the current streak runs from
the latest of those. Tap a counter to open its editor and the full list of
slips is there, newest first, each with a ✕ — removing one restores the streak
that preceded it, so a mis-tapped reset costs nothing.

The widget's list gets a single `PendingIntent` template for all its rows, so
the icon and the row cannot point at different activities. Both go to
`WidgetTapActivity` with different fill-in extras; it is translucent, so
resetting from the widget shows a dialog without leaving the home screen.

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

Colours come from a picker with a saturation/value square, a hue strip, an
opacity strip (background only), eight quick swatches, and a `#RRGGBB` /
`#AARRGGBB` hex field — all driven off one HSVA value, so they stay in sync. The unit word ("days", "weeks") is drawn at 65% of the
name colour's alpha, so it recedes without needing a fourth setting. Row
spacing also drives the widget's outer padding and the rows' horizontal
padding, so a single slider tightens the whole thing.

**Number format.** Counters store the exact moment sobriety started, not just
the date, so the first day reads in minutes and then hours instead of sitting
at "0 days" until midnight. *Adaptive* picks the largest unit that still reads
honestly:

| Elapsed | Shown |
| --- | --- |
| under 1 hour | `42 minutes` |
| under 1 day | `5 hours` |
| 1–6 days | `5 days` |
| 7–55 days | `3 weeks` |
| 56–364 days | `4 months` |
| 365 days+ | `2 years` |

*Days* stops converting past the first day — `128 days`, `730 days` — but still
reads in minutes and hours before day one is up, since that is the whole point
of keeping the start time. Either way the number and the unit are separate
views, so they get real spacing between them and can be coloured apart.

Both columns are pinned to a common width each refresh, measured from the
widest number and the widest unit actually on screen, so every row's number
starts at the same x and so does every unit. A fixed dp would break the moment
the text size setting moved. The widths go out as `setMinWidth`, which
RemoteViews has been able to call since API 24; `setGravity` only became
remotable later, so the alignment inside each column lives in the layout XML.

Elapsed time is measured as a duration, not in calendar days: a counter started
at 3pm turns over at 3pm, not at midnight. That also sidesteps daylight saving
entirely.

There is no Save button — every change is written through and repaints the
widget straight away (slider changes commit when your finger lifts). A settings
screen that stages changes behind a button loses them silently when you back
out of it.

All of it is applied at runtime — `setTextViewTextSize`, `setViewPadding`,
`setTextColor`, and a tinted background image — rather than being baked into
the layout, so placed widgets update with no reinstall and no re-adding. The layout XML carries the same values as defaults, which
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

## Staying up to date

Sideloaded apps get no updates on their own. Two ways to stop re-downloading by
hand, in order of least work:

**Obtainium** (recommended). Install [Obtainium][obtainium], add this repo's
URL, and it watches GitHub Releases and offers each new build. Grant it
"install unknown apps" once and updating is a single tap.

**F-Droid with a self-hosted repo.** More setup, but F-Droid can check on a
schedule and, on a rooted device with the Privileged Extension, install
silently.

Either way the updates come from GitHub Releases, which
`.github/workflows/android-release.yml` publishes. Tag a commit to cut one:

```
# bump android:versionCode and android:versionName in AndroidManifest.xml first
git tag v1.6 && git push origin v1.6
```

The workflow refuses to run if the tag and `versionName` disagree, so a
forgotten bump fails loudly instead of shipping an APK that cannot install over
the last one.

### The signing key must not change

Android refuses to install an update whose signature differs from the installed
app, so every release has to be signed with the same key. The workflow reads it
from two repository secrets:

| Secret | |
| --- | --- |
| `SOBER_KEYSTORE_BASE64` | `base64 -w0 android/debug.p12` |
| `SOBER_KEYSTORE_PASSWORD` | the keystore password (`android` by default) |

`build.sh` picks the key up from `SOBER_KEYSTORE` / `SOBER_KEYSTORE_PASSWORD` /
`SOBER_KEYSTORE_ALIAS` when they are set, and otherwise generates a local
throwaway key at `android/debug.p12`. **Back that file up.** It is git-ignored,
and losing it means every later build is a different app as far as Android is
concerned — the only way back is to uninstall and lose your counters.

`verify.sh` prints the signer's SHA-256, which is how you confirm two APKs will
update each other.

[obtainium]: https://github.com/ImranR98/Obtainium

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

- The widget schedules its own wake-up for the moment its soonest counter
  changes value — a minute apart in the first hour, an hour apart in the first
  day, then daily. `updatePeriodMillis` bottoms out at 30 minutes, which is no
  use to a counter reading in minutes; it stays configured at an hour purely as
  a backstop. The alarm is inexact on purpose, since exact alarms need a
  permission prompt on Android 12+ that a widget does not deserve.
- The list in the app refreshes every 20 seconds while it is on screen, and
  stops when it is not.
- `Store.notifyWidgets` repaints widgets by calling `updateAppWidget` directly
  rather than broadcasting `ACTION_APPWIDGET_UPDATE` to our own receiver. Row
  contents reach the widget through the `RemoteViewsFactory`, but the frame —
  background colour, outer padding — is only set in the provider's render pass,
  so a dropped broadcast used to leave the background stale while the rows
  updated correctly.
- Data lives only on the device, in `SharedPreferences`. There is no network
  permission in the manifest.
