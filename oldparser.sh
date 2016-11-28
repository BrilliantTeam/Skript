#!/bin/bash
# Builds the old parser version of Skript

cp -R src src_store
cp -R oldparser/* src
grep -rl 'api' src/ | xargs sed -i 's/api/GREP_TOKEN/g'
grep -rl 'pi\.config' src/ | xargs sed -i 's/pi\.config/ScriptLoader\.currentScript/g'
grep -rl 'pi\.' src/ | xargs sed -i 's/pi\./ScriptLoader\./g'
grep -rl '(pi, ' src/ | xargs sed -i 's/(pi, /(/g'
grep -rl ', pi)' src/ | xargs sed -i 's/, pi)/)/g'
grep -rl ', final ParserInstance pi' src/ | xargs sed -i 's/, final ParserInstance pi//g'
grep -rl 'GREP_TOKEN' src/ | xargs sed -i 's/GREP_TOKEN/api/g'

export SKRIPT_JAR_NAME="Skript-oldparser.jar"
./gradlew build
rm -r src
mv src_store src
