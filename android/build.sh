#!/usr/bin/env bash
# Builds Sober.apk without Android Studio or the Android SDK installer.
#
# The pieces it needs (android.jar, aapt2, dx, apksig) are fetched from
# Maven Central and GitHub into $TOOLS on first run.
set -euo pipefail

unset JAVA_TOOL_OPTIONS  # keeps the proxy banner out of the build log

HERE="$(cd "$(dirname "$0")" && pwd)"
TOOLS="${ANDROID_MINI_TOOLS:-$HOME/atools}"
BUILD="$HERE/build"
OUT="$HERE/out"

MIN_SDK=24
TARGET_SDK=34
PKG=com.oliverstoll.sobriety

AAPT2="$TOOLS/aapt2"
ANDROID_JAR="$TOOLS/android-34.jar"
DX_JAR="$TOOLS/dalvik-dx.jar"
APKSIG_JAR="$TOOLS/apksig.jar"

fetch() {
    [ -f "$2" ] || { echo "fetching $(basename "$2")"; curl -sSLf -o "$2" "$1"; }
}

mkdir -p "$TOOLS"
fetch "https://raw.githubusercontent.com/Sable/android-platforms/master/android-34/android.jar" "$ANDROID_JAR"
fetch "https://repo1.maven.org/maven2/com/jakewharton/android/repackaged/dalvik-dx/16.0.1/dalvik-dx-16.0.1.jar" "$DX_JAR"
fetch "https://repo1.maven.org/maven2/com/android/tools/build/apksig/2.3.0/apksig-2.3.0.jar" "$APKSIG_JAR"
if [ ! -x "$AAPT2" ]; then
    echo "fetching aapt2"
    fetch "https://repo1.maven.org/maven2/org/apktool/apktool-lib/3.0.3/apktool-lib-3.0.3.jar" "$TOOLS/apktool-lib.jar"
    unzip -o -q -j "$TOOLS/apktool-lib.jar" prebuilt/linux/aapt2 -d "$TOOLS"
    chmod +x "$AAPT2"
fi

rm -rf "$BUILD"
mkdir -p "$BUILD/gen" "$BUILD/classes" "$OUT"

echo "==> aapt2 compile"
"$AAPT2" compile --dir "$HERE/res" -o "$BUILD/res.zip"

echo "==> aapt2 link"
"$AAPT2" link \
    -I "$ANDROID_JAR" \
    --manifest "$HERE/AndroidManifest.xml" \
    --java "$BUILD/gen" \
    --min-sdk-version "$MIN_SDK" \
    --target-sdk-version "$TARGET_SDK" \
    -0 arsc \
    -o "$BUILD/base.apk" \
    "$BUILD/res.zip"

echo "==> javac"
find "$HERE/src" "$BUILD/gen" -name '*.java' > "$BUILD/sources.txt"
# Run javac without a pipe: piping loses its exit status, and a partial
# compile would otherwise sail through and be packaged into a broken APK.
if ! javac -nowarn -encoding UTF-8 \
        -source 8 -target 8 -bootclasspath "$ANDROID_JAR" \
        -d "$BUILD/classes" @"$BUILD/sources.txt" > "$BUILD/javac.log" 2>&1; then
    grep -v "bootstrap class path" "$BUILD/javac.log" >&2
    echo "javac failed" >&2
    exit 1
fi
grep -v "bootstrap class path" "$BUILD/javac.log" || true

echo "==> dex"
java -cp "$DX_JAR" com.android.dx.command.Main \
    --dex --min-sdk-version="$MIN_SDK" \
    --output="$BUILD/classes.dex" "$BUILD/classes"

echo "==> package"
cp "$BUILD/base.apk" "$BUILD/unsigned.apk"
(cd "$BUILD" && zip -q -X "unsigned.apk" classes.dex)

echo "==> align"
python3 "$HERE/align.py" "$BUILD/unsigned.apk" "$BUILD/aligned.apk"

echo "==> sign"
# Android refuses to update an installed app whose signature differs, so every
# build that is meant to upgrade an earlier one must reuse the same keystore.
# CI passes the shared key in through SOBER_KEYSTORE; a local build falls back
# to a throwaway key it generates once.
KEYSTORE="${SOBER_KEYSTORE:-$HERE/debug.p12}"
KEYSTORE_PASSWORD="${SOBER_KEYSTORE_PASSWORD:-android}"
KEYSTORE_ALIAS="${SOBER_KEYSTORE_ALIAS:-sober}"

if [ ! -f "$KEYSTORE" ]; then
    if [ -n "${SOBER_KEYSTORE:-}" ]; then
        echo "SOBER_KEYSTORE is set but $KEYSTORE does not exist" >&2
        exit 1
    fi
    echo "    generating a local signing key at $KEYSTORE"
    keytool -genkeypair -v -storetype PKCS12 -keystore "$KEYSTORE" \
        -alias "$KEYSTORE_ALIAS" -keyalg RSA -keysize 2048 -validity 10950 \
        -storepass "$KEYSTORE_PASSWORD" -keypass "$KEYSTORE_PASSWORD" \
        -dname "CN=Sober, OU=Dev, O=Sober, L=Berlin, C=DE" >/dev/null
fi
javac -nowarn -cp "$APKSIG_JAR" -d "$BUILD/tools" "$HERE/tools/Signer.java"
# apksig 2.3.0 reaches into sun.security.x509, sealed off since JDK 9.
java --add-exports java.base/sun.security.x509=ALL-UNNAMED \
    --add-exports java.base/sun.security.pkcs=ALL-UNNAMED \
    --add-exports java.base/sun.security.util=ALL-UNNAMED \
    -cp "$APKSIG_JAR:$BUILD/tools" Signer \
    "$BUILD/aligned.apk" "$OUT/Sober.apk" \
    "$KEYSTORE" "$KEYSTORE_PASSWORD" "$KEYSTORE_ALIAS" "$MIN_SDK"

echo
VERSION_NAME="$(sed -n 's/.*android:versionName="\([^"]*\)".*/\1/p' "$HERE/AndroidManifest.xml" | head -1)"
VERSION_CODE="$(sed -n 's/.*android:versionCode="\([^"]*\)".*/\1/p' "$HERE/AndroidManifest.xml" | head -1)"
echo "APK: $OUT/Sober.apk  (v$VERSION_NAME, code $VERSION_CODE, $(du -h "$OUT/Sober.apk" | cut -f1))"
