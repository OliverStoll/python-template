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
| `src/…/Settings.java` | Widget text size and row spacing |
| `src/…/Days.java` | Calendar-day arithmetic that survives DST |
| `build.sh` | Builds `out/Sober.apk` |
| `verify.sh` | Checks the APK's signature, alignment and manifest |

No Gradle, no AndroidX, no third-party runtime dependencies — just the platform
framework, so the whole APK is 28 KB.

## Widget appearance

The gear in the app's header opens **Widget appearance**: two sliders over a
live preview of a widget row.

| | Range | Default |
| --- | --- | --- |
| Text size | 8–22 sp | 13 sp |
| Row spacing | 0–14 dp | 3 dp |

Row spacing also drives the widget's outer padding (one step wider) and the
rows' horizontal padding, so a single slider tightens the whole thing. The
emoji sits two points above the text size.

Both are applied at runtime via `RemoteViews.setTextViewTextSize` and
`setViewPadding` rather than being baked into the layout, so saving redraws
placed widgets immediately — no reinstall, no re-adding the widget. The values
in the layout XML are the defaults above, which keeps the first frame correct
before the adapter runs.

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
