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
javac -nowarn -encoding UTF-8 \
    -source 8 -target 8 -bootclasspath "$ANDROID_JAR" \
    -d "$BUILD/classes" @"$BUILD/sources.txt" 2>&1 | grep -v "bootstrap class path" || true
[ -n "$(find "$BUILD/classes" -name '*.class' -print -quit)" ] || { echo "javac produced no classes"; exit 1; }

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
KEYSTORE="$HERE/debug.p12"
if [ ! -f "$KEYSTORE" ]; then
    keytool -genkeypair -v -storetype PKCS12 -keystore "$KEYSTORE" \
        -alias sober -keyalg RSA -keysize 2048 -validity 10950 \
        -storepass android -keypass android \
        -dname "CN=Sober Debug, OU=Dev, O=Sober, L=Berlin, C=DE" >/dev/null
fi
javac -nowarn -cp "$APKSIG_JAR" -d "$BUILD/tools" "$HERE/tools/Signer.java"
# apksig 2.3.0 reaches into sun.security.x509, sealed off since JDK 9.
java --add-exports java.base/sun.security.x509=ALL-UNNAMED \
    --add-exports java.base/sun.security.pkcs=ALL-UNNAMED \
    --add-exports java.base/sun.security.util=ALL-UNNAMED \
    -cp "$APKSIG_JAR:$BUILD/tools" Signer \
    "$BUILD/aligned.apk" "$OUT/Sober.apk" "$KEYSTORE" android sober "$MIN_SDK"

echo
echo "APK: $OUT/Sober.apk  ($(du -h "$OUT/Sober.apk" | cut -f1))"
