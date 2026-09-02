#!/usr/bin/env bash
# Runs the plain-JVM tests over the logic that does not need a device.
set -euo pipefail
unset JAVA_TOOL_OPTIONS

HERE="$(cd "$(dirname "$0")" && pwd)"
TOOLS="${ANDROID_MINI_TOOLS:-$HOME/atools}"
OUT="$HERE/build/test"

# The tests reference R, which only exists after a link pass.
[ -n "$(find "$HERE/build/gen" -name R.java 2>/dev/null)" ] || "$HERE/build.sh" >/dev/null

# A real org.json, first on the classpath: android.jar's copy is a stub that
# throws, and the JSON round-trip is exactly what must not break for anyone
# with counters already saved.
JSON_JAR="$TOOLS/json.jar"
[ -f "$JSON_JAR" ] || curl -sSLf -o "$JSON_JAR" \
    "https://repo1.maven.org/maven2/org/json/json/20240303/json-20240303.jar"

mkdir -p "$OUT"
javac -nowarn -encoding UTF-8 -cp "$JSON_JAR:$TOOLS/android-34.jar" -d "$OUT" \
    "$HERE"/src/com/oliverstoll/sobriety/*.java \
    $(find "$HERE/build/gen" -name R.java) \
    "$HERE"/test/com/oliverstoll/sobriety/*.java

java -cp "$OUT:$JSON_JAR:$TOOLS/android-34.jar" com.oliverstoll.sobriety.Tests
