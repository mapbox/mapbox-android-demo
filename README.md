# Mapbox Android Demo app

![](https://github.com/mapbox/mapbox-android-demo/blob/master/screenshots/splash.png)

[![Build Status](https://www.bitrise.io/app/9778f1e5f744661f.svg?token=bNy-zoctPHivmB6e8inudA&branch=master)](https://www.bitrise.io/app/9778f1e5f744661f)

This is a public demo of the Mapbox Maps SDK for Android. The demo app is [available now in the Google Play Store](https://play.google.com/store/apps/details?id=com.mapbox.mapboxandroiddemo) and it shows off all the examples found on [mapbox.com/android-sdk/examples](https://www.mapbox.com/android-sdk/examples/) using the same code. There are also examples that only live in the app. The app "labs" section combines the examples to create more complex demos. Check out [the overview page](https://www.mapbox.com/android-sdk/) to get started using the Mapbox Maps SDK for Android in your Android project.

### Steps to adding a new example
Feedback and contribution is encouraged in this repo, if you'd like to see a new example added in the app either [open an issue](https://github.com/mapbox/mapbox-android-demo/issues) or create it yourself and open a pull request following these steps:

**Note:** This repo uses checkstyle to make sure code contributed follows the Mapbox Style standards. When a pull request is opened, Bitrise will check that no style issues occur in the code. To setup Checkstyle in Android Studio read this [wiki entry](https://github.com/mapbox/mapbox-android-demo/wiki/Setting-up-Mapbox-checkstyle).

* Create a new java file and place in the acceptable folder (`examples` or `labs`) giving it a descriptive name and ending the title with `*Activity`. It's important that all activities extend `AppCompat`.
* If the activity needs a layout, add one with the naming convention `activity_*`.
* Add the activity to the `AndroidManifest.xml` file.
* Open the `MainActivity.java` file and scroll down to the `listItems` method. You'll notice a switch statement, pick one of the cases (categories) and add the new example so it matches the others. 
* Code your example and test to make sure it works properly.
* Open a pull request with the new example.

### Screenshots
<img src="https://github.com/mapbox/mapbox-android-demo/blob/master/screenshots/phone/main-activity.png" width="360">
<img src="https://github.com/mapbox/mapbox-android-demo/blob/master/screenshots/phone/navigation-drawer.png" width="360">
<img src="https://github.com/mapbox/mapbox-android-demo/blob/master/screenshots/phone/custom-marker-example.png" width="360">
<img src="https://github.com/mapbox/mapbox-android-demo/blob/master/screenshots/phone/following-route.png" width="360">
<img src="https://github.com/mapbox/mapbox-android-demo/blob/master/screenshots/phone/user-location.png" width="360">
