MapBoxAndroidDemo
=================

Demo application using the MapBox Android SDK in AAR packaging from a local Maven Repository.

How to Install MapBox Android SDK to Local Maven
1 Clone https://github.com/mapbox/mapbox-android-sdk to local machine
2 From local directory where source was cloned run the following command

```sh
gradle clean assembleRelease install
```

3 Add MapBox Android SDK as a dependency in your Android project's build.gradle

```
repositories {
    mavenLocal()
}

dependencies {
	compile 'com.mapbox.mapboxsdk:Mapbox-Android-SDK:1.0.0-SNAPSHOT@aar'
}
```
