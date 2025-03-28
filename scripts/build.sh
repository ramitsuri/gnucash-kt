#!/bin/bash

./gradlew clean
./gradlew shadowJar
mv build/libs/GnuCashReports-1.0-SNAPSHOT-all.jar build/libs/app.jar