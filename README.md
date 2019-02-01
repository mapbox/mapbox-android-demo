# Mapbox Android Demo app

![](https://github.com/mapbox/mapbox-android-demo/blob/master/screenshots/splash.png)

[![CircleCI](https://circleci.com/gh/mapbox/mapbox-android-demo.svg?style=svg)](https://circleci.com/gh/mapbox/mapbox-android-demo)

This is a public demo of the Mapbox Maps SDK for Android. The demo app is [available now in the Google Play Store](https://play.google.com/store/apps/details?id=com.mapbox.mapboxandroiddemo) and it shows off many of the examples found on:

- [mapbox.com/android-docs/map-sdk/examples](https://www.mapbox.com/android-docs/map-sdk/examples/)
- [mapbox.com/android-docs/navigation/examples/](https://www.mapbox.com/android-docs/navigation/examples/)
- [mapbox.com/android-docs/java/examples/](https://www.mapbox.com/android-docs/java/examples/)
- [mapbox.com/android-docs/plugins/examples/](https://www.mapbox.com/android-docs/plugins/examples/)


There are also examples that only live in the app. The app's "labs" section has examples which often combine various Mapbox tools to create more complex examples.

Visit [the overview page](https://www.mapbox.com/android-sdk/) to get started using the Mapbox Maps SDK for Android in your Android project.

### Steps to adding a new example
Feedback and contribution are encouraged in this repo. If you'd like to see a new example added into the app, either [open an issue](https://github.com/mapbox/mapbox-android-demo/issues) or create it yourself and open a pull request following these steps:

* Create a new java file and place in the acceptable folder (`examples` or `labs`) giving it a descriptive name and ending the title with `*Activity`. It's important that all activities extend `AppCompat`.
* If the activity needs a layout, add one with the naming convention `activity_*`.
* Add the activity to the `AndroidManifest.xml` file.
* Open the `MainActivity.java` file and scroll down to the `listItems` method. You'll notice a switch statement, pick one of the cases (categories) and add the new example so it matches the others. 
* Code your example and test to make sure it works properly.
* Open a pull request with the new example.

**Note:** This repo uses checkstyle to make sure code contributed follows the Mapbox Style standards. When a pull request is opened, Bitrise will check that no style issues occur in the code. Read this [wiki entry](https://github.com/mapbox/mapbox-android-demo/wiki/Setting-up-Mapbox-checkstyle) to set up checkstyle in Android Studio.


### Running locally

##### Setting the Mapbox access token

_This demo app uses Mapbox vector tiles, which require a Mapbox account and a Mapbox access token. Obtain a free access token on the [Mapbox account page](https://www.mapbox.com/studio/account/tokens/)._

With the first Gradle invocation, Gradle will take the value of the `MAPBOX_ACCESS_TOKEN` environment variable and save it to `SharedCode/src/main/res/values/developer-config.xml`. If the environment variable wasn't set, you can create/edit the `developer-config.xml` file. Create an `access_token` String resource and paste your access token into it:

```xml
<string name="access_token">PASTE_YOUR_TOKEN_HERE</string>
```

##### Flavoring

The app has a `global` and a `china` flavor. You'll probably want the `global` flavor, which has the full set of demo app examples. Make sure to switch to the `global` build variant if you're seeing messages and warnings about `ChinaMapView`/etc.

The `china` flavor uses [the Mapbox China Plugin for Android](https://docs.mapbox.com/android/plugins/overview/china/) and requires a special China access token. Contact Mapbox via [https://www.mapbox.cn/contact](https://www.mapbox.cn/contact) to start the process of receiving a China access token.

### Inside the app

<img src="https://user-images.githubusercontent.com/4394910/42973575-7ade2f44-8b68-11e8-9fa4-341c35171b92.gif" width="325"/> <img src="https://user-images.githubusercontent.com/4394910/42974229-3d6061f2-8b6b-11e8-8c27-be58a0a334cf.gif" width="325"/> 

<img src="https://user-images.githubusercontent.com/4394910/42974704-32f243aa-8b6d-11e8-8f8d-3b47a889f440.gif" width="325"/> <img src="https://user-images.githubusercontent.com/4394910/42974764-832ac1d0-8b6d-11e8-9ca8-cb259471690b.gif" width="325"/> 
