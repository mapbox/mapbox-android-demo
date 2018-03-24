# Release documentation

This doc covers the steps required to release a new version of [Mapbox Demo App](https://play.google.com/store/apps/details?id=com.mapbox.mapboxandroiddemo) to Google Play:

## Prepare code

Standard workflow:
 - Update and integrate dependencies
 - Make sure that you've [installed the Transifex client](https://docs.transifex.com/client/installing-the-client). Navigate to this repo's root folder in your command line (Terminal, etc.) and run `tx pull -a` to update the app's string files. This will pull in the latest string file translations from [Transifex](transifex.com).
 - Update `whatsnew/whatsnew-en-US` with the additions that were made to the app since the last release.
 - Local test with proguard 
 - Merge PR with changes to the `master` branch in this repo.

## Create a tag

Create a tag from master [here](https://github.com/mapbox/mapbox-android-demo/tags).

## Start CI build

Navigate to `Bitrise` > `mapbox-android-demo` and select `start/schedule a build`: 
 - Build from branch or tag
 - Select `publish` in the workflow dropdown
 - Press `Start build` 
 
If successful CI will upload multiple APK files to the alpha channel of Google Play.
We are uploading multiple apk files due to [ABI splits](https://developer.android.com/studio/build/configure-apk-splits.html#configure-abi-split) and [android wear integration](https://developer.android.com/training/wearables/apps/packaging.html)).
 
## Test application on Google Play

Enroll yourself in the testing group in Google Play console. 
Test the application in alpha or beta tracks and eventually promote the build to production.
