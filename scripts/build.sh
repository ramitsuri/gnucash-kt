#!/bin/bash

./gradlew clean
./gradlew shadowJar
mv build/libs/*-all.jar build/libs/app.jar

toml_file="./gradle/libs.versions.toml"
property="appVersion"
app_version=$(grep "^$property = " "$toml_file" | sed -E 's/^[^"]*"([^"]*)".*/\1/')
if [ -z "$app_version" ]; then
  echo "Error: Property '$property' not found in '$toml_file'"
  exit 1
fi

echo "$app_version" > app_version.txt