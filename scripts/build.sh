#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")/.."
mkdir -p build/classes
find src/main/java -name '*.java' | sort > build/sources.txt
javac --release 17 -encoding UTF-8 -Xlint:all -Werror -d build/classes @build/sources.txt
echo 'Build passed (Java 17 target).'
