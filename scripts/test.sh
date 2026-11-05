#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")/.."
sh scripts/build.sh
mkdir -p build/test-classes
find src/test/java -name '*.java' | sort > build/test-sources.txt
javac --release 17 -encoding UTF-8 -Xlint:all -Werror -cp build/classes -d build/test-classes @build/test-sources.txt
java -cp 'build/classes:build/test-classes' oopnemo.CampusServiceTest
