MapBoxAndroidDemo
=================

Demo application using the [MapBox Android SDK](https://github.com/mapbox/mapbox-android-sdk) in
AAR packaging from a local Maven Repository.

How to install MapBox Android SDK to Local Maven

```sh
# build sdk
$ git clone https://github.com/mapbox/mapbox-android-sdk
$ cd mapbox-android-sdk
$ gradle clean assembleRelease install
```

Then add MapBox Android SDK as a dependency in your Android project's build.gradle Ex: `MapBoxAndroidDemo/build.gradle`

```
repositories {
    mavenLocal()
}

dependencies {
	compile 'com.mapbox.mapboxsdk:mapbox-android-sdk:1.0.0-SNAPSHOT@aar'
}
```

The string `mapbox-android-sdk` varies based on the **directory name of your SDK source**,
so adjust it if you aren't using the default.

*Satellite*

![Satellite](https://raw2.github.com/bleege/MapBoxAndroidDemo/master/20140209-satellite.png)

*Streets*

![Streets](https://raw2.github.com/bleege/MapBoxAndroidDemo/master/20140209-streets.png)

*Terrain*

![Terrain](https://raw2.github.com/bleege/MapBoxAndroidDemo/master/20140209-terrain.png)
