#!/bin/sh
# this script just joins together the commands
# you need to build the SDK from the cli, install it on device,
# and run it.
../gradlew clean assemble
adb shell am start -n "com.mapbox.mapboxandroiddemo/com.mapbox.mapboxandroiddemo.MainActivity" -a android.intent.action.MAIN -c android.intent.category.LAUNCHER
