#!/bin/bash

./gradlew clean
./gradlew shadowJar
mv build/libs/*-all.jar build/libs/app.jar