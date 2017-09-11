#!/bin/bash
# Compiles Skript with some special settings used to produce unstable builds

SKRIPT_VER="$(git rev-parse --short HEAD)-unstable"
export SKRIPT_VERSION=$SKRIPT_VER

./gradlew clean build

echo "$SKRIPT_VER" >buildbot-upload-version.txt
