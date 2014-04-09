#!/bin/sh

keytool -genkey -v -keystore mapbox_demo.keystore -alias mapbox_demo -keyalg RSA -keysize 2048 -validity 10000
