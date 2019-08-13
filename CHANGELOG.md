# Changelog for Mapbox Android Demo app

Mapbox welcomes participation and contributions from everyone.

## 8.2.1 - August 7, 2019

* Added example of cache management methods usage #[1139](https://github.com/mapbox/mapbox-android-demo/pull/1139)
* Bumped maps sdk to 8.2.1 #[1155](https://github.com/mapbox/mapbox-android-demo/pull/1155)
* Scalebar plugin bump to 0.2.0 #[1154](https://github.com/mapbox/mapbox-android-demo/pull/1154)
* Bumped Maps SDK to stable 8.2.0 #[1129 ](https://github.com/mapbox/mapbox-android-demo/pull/1129 )
* Refactoring GeoJsonSource creation with URL to URI #[1150](https://github.com/mapbox/mapbox-android-demo/pull/1150)
* Refactor QueryFeatureActivity to use SymbolLayer instead of MarkerViewOptions #[1148](https://github.com/mapbox/mapbox-android-demo/pull/1148)
* Adjusted manifest to fix SimpleMapView kotlin example #[1149](https://github.com/mapbox/mapbox-android-demo/pull/1149)
* Add gradle.properties file to fix compile error. #[1147](https://github.com/mapbox/mapbox-android-demo/pull/1147)
* Define a Circle's Radius with Physical Units #[1047 ](https://github.com/mapbox/mapbox-android-demo/pull/1047 )
* Polygon hole layer below null check #[1145](https://github.com/mapbox/mapbox-android-demo/pull/1145)
* Adding kotlin lint plugin and needed tweaks #[1140](https://github.com/mapbox/mapbox-android-demo/pull/1140)
* Fix NPE #[1130](https://github.com/mapbox/mapbox-android-demo/pull/1130)
* Adding Firebase crashlytics and AndroidX support #[1104](https://github.com/mapbox/mapbox-android-demo/pull/1104)
* Add tutorial demos #[1127](https://github.com/mapbox/mapbox-android-demo/pull/1127))
* Added RecyclerView + Directions route example #[1123](https://github.com/mapbox/mapbox-android-demo/pull/1123)
* Refactoring fromUrl() to fromUri() #[1120](https://github.com/mapbox/mapbox-android-demo/pull/1120)
* Adding interactive isochrone + seekbar slider example #[1121](https://github.com/mapbox/mapbox-android-demo/pull/1121)
* Removing a duplicate globalImplementation dependenciesList.firebaseCrash line #[1115](https://github.com/mapbox/mapbox-android-demo/pull/1115)
* Adjusted strings for trailing line example and dashed directions example ##[111](https://github.com/mapbox/mapbox-android-demo/pull/111)

## 8.1.0 - June 21, 2019

* Adding LocationComponent camera mode example #[1038](https://github.com/mapbox/mapbox-android-demo/pull/1038)
* Drawn line behind moving SymbolLayer icon #[998](https://github.com/mapbox/mapbox-android-demo/pull/998)
* Fix index out of bound exception #[1108](https://github.com/mapbox/mapbox-android-demo/pull/1108)
* Bumped maps sdk to 8.1.0 #[1088](https://github.com/mapbox/mapbox-android-demo/pull/1088)

## 8.0.1 - June 13, 2019

* Variable label placement example #[1066](https://github.com/mapbox/mapbox-android-demo/pull/1066)
* Adding example of "revealed" polygon hole with outline #[1050](https://github.com/mapbox/mapbox-android-demo/pull/1050)
* Adding draw on map for search example #[983](https://github.com/mapbox/mapbox-android-demo/pull/983)
* Refactoring Isochrone example to add time label toggle #[1087](https://github.com/mapbox/mapbox-android-demo/pull/1087)
* Adding Scale Bar Plugin example #[1103](https://github.com/mapbox/mapbox-android-demo/pull/1103)
* China plugin bump to 2.2.0 #[1081](https://github.com/mapbox/mapbox-android-demo/pull/1081)
* Making needed iconTranslate -> iconOffset refactoring changes #[1084](https://github.com/mapbox/mapbox-android-demo/pull/1084)
* Added geometry check to building outline example #[1086](https://github.com/mapbox/mapbox-android-demo/pull/1086)
* Moving Firebase crash configuration to CI #[1089](https://github.com/mapbox/mapbox-android-demo/pull/1089)
* Fixes to various small issues found during QA #[1099](https://github.com/mapbox/mapbox-android-demo/pull/1099)
* Bumped plugins as part of giant plugin release following Maps SDK 8.0.0 release #[1102](https://github.com/mapbox/mapbox-android-demo/pull/1102)
* Bumped Maps SDK to 8.0.1 #[1105](https://github.com/mapbox/mapbox-android-demo/pull/1105)

## 8.0.0

* bump Maps SDK to v7.4.0 #[1061](https://github.com/mapbox/mapbox-android-demo/pull/1061)
* get only fully loaded style when returning from async tasks #[1019](https://github.com/mapbox/mapbox-android-demo/pull/1019)
* Fixed description for collision detection example #[1075](https://github.com/mapbox/mapbox-android-demo/pull/1075) 
* Adding example of onStyleImageMissing listener usage #[1070](https://github.com/mapbox/mapbox-android-demo/pull/1070)
* Adding ignore sign in button #[1033](https://github.com/mapbox/mapbox-android-demo/pull/1033) 
* Adjusted HomeScreenWidgetActivity class' location and its javadocs #[1031](https://github.com/mapbox/mapbox-android-demo/pull/1031)
* Fix StringIndexOutOfBounds for urls that do not have `?` #[1073](https://github.com/mapbox/mapbox-android-demo/pull/1073) 
* Tile loading performance measurement #[1012](https://github.com/mapbox/mapbox-android-demo/pull/1012) 
* Adjusted GeojsonLayerInStackActivity example card image #[1067](https://github.com/mapbox/mapbox-android-demo/pull/1067) 
* Camera adjustment to GeojsonLayerInStackActivity #[1064](https://github.com/mapbox/mapbox-android-demo/pull/1064) 
* Adding coordinate check in IsochroneActivity #[1065](https://github.com/mapbox/mapbox-android-demo/pull/1065) 
* Adding example of ignorePlacement/allowOverlap for text and icons #[1063](https://github.com/mapbox/mapbox-android-demo/pull/1063) 
* Switching to greater than or equals for circle clustering example #[1049](https://github.com/mapbox/mapbox-android-demo/pull/1049) 
* Refactoring and splitting basic SymbolLayer icon examples #[1030](https://github.com/mapbox/mapbox-android-demo/pull/1030)
* Adding example of using TurfTransformation#circle to create visual ring #[1039](https://github.com/mapbox/mapbox-android-demo/pull/1039)
* bumped turf and services to 4.8.0 #[1062](https://github.com/mapbox/mapbox-android-demo/pull/1062) 
* Updated Mapbox SF office coordinates for PlacesPluginActivity #[1041](https://github.com/mapbox/mapbox-android-demo/pull/1041) 
* Adding multiple color formatting to TextFieldMultipleFormatsActivity #[1054](https://github.com/mapbox/mapbox-android-demo/pull/1054)
* Isochrone API refactor to use Java SDK wrapper #[1029](https://github.com/mapbox/mapbox-android-demo/pull/1029)

## 7.4.0-beta.2

* Adding SKU generation (via Maps SDK bump to 7.4.0-beta.2) #[1043](https://github.com/mapbox/mapbox-android-demo/pull/1043)
* Refactoring #setGeoJson() to pass in Point object directly as a parameter #[1040](https://github.com/mapbox/mapbox-android-demo/pull/1040)
* Adding note about product flavors to README #[987](https://github.com/mapbox/mapbox-android-demo/pull/987)

## 7.3.3 (This release corresponds to v7.3.2 release of the Maps SDK)
* Refactoring LocationComponent examples to use LocationComponentActivationOptions#[1010](https://github.com/mapbox/mapbox-android-demo/pull/1010)
* Bumped dependencies for new plugin releases #[1014](https://github.com/mapbox/mapbox-android-demo/pull/1014)
* Bumped china plugin to 2.1.1 #[1026](https://github.com/mapbox/mapbox-android-demo/pull/1026)
* Fixes waving bear's image decoding on Android Pie #[1025](https://github.com/mapbox/mapbox-android-demo/pull/1025)
* Using dependencies.gradle file for Gradle plugin script version setup #[1017](https://github.com/mapbox/mapbox-android-demo/pull/1017)
* Removed duplicate snaking directions line example #[1027](https://github.com/mapbox/mapbox-android-demo/pull/1027)
* Bumped turf and services to 4.6.0 #[1028](https://github.com/mapbox/mapbox-android-demo/pull/1028)
* Tweaks to SymbolLayer icon offset spacing #[1016](https://github.com/mapbox/mapbox-android-demo/pull/1016)
* Bumped maps sdk to 7.3.2 #[1035](https://github.com/mapbox/mapbox-android-demo/pull/1035)
* Fix to geocoding example XML #[1006](https://github.com/mapbox/mapbox-android-demo/pull/1006), #[1007](https://github.com/mapbox/mapbox-android-demo/pull/1007)

## 7.3.0

* Multiple text field format
* Symbol listener
* Click to add image
* Symbol switch based on zoom level
* Rotating map camera
* Animated SymbolLayer icon


## 7.2.0

* Added manifest declaration to use CleartextTraffic #[980](https://github.com/mapbox/mapbox-android-demo/pull/980)
* Removing GIF library #[979](https://github.com/mapbox/mapbox-android-demo/pull/979)
* Maps SDK v7.2.0 #[978](https://github.com/mapbox/mapbox-android-demo/pull/978)
* Fix disappearing camera restriction bbox #[972](https://github.com/mapbox/mapbox-android-demo/pull/972)

## 7.1.2

* Localization plugin 0.8.0 bump #[960](https://github.com/mapbox/mapbox-android-demo/pull/960)
* Add data point icon as an SDF in the CircleLayerClusteringActivity #[964](https://github.com/mapbox/mapbox-android-demo/pull/964 )
* Place search SF office address change #[941](https://github.com/mapbox/mapbox-android-demo/pull/941)
* Replacing all Log. with Timber #[956](https://github.com/mapbox/mapbox-android-demo/pull/956)
* Added wearable declaration tag to manifest #[963 ](https://github.com/mapbox/mapbox-android-demo/pull/963 )
* Fixing wearable black screen #[627](https://github.com/mapbox/mapbox-android-demo/pull/627)

## 7.0.0

* Maps SDK 7.0.0 bump

## 6.8.1

* Maps SDK 6.8.1 bump

## 6.8.0

* Maps SDK 6.8.0 bump
* LocationComponent options example
* Background fog example
* Java SDK 4.3.0 bump

## 6.7.1

* Play store setup gradle tweaks
* Maps SDK 6.7.1 bump
 
## 6.7.0

* CI Play Store release adjustment
* Proguard rule adjustment
* Maps SDK 6.7.0 bump
* Isochrone API
* Line gradient
* Satellite land selection
* Click-to-add-bounds image
* Dropped gradle from 3.2.1 to 3.1.0
* Added LoadingActivity back to global flavor manifest
* Java SDK bump to 4.1.0

## 6.6.4

* Maps SDK 6.6.4 bump 
* Maps SDK 6.6.3 bump 
* Maps SDK 6.6.2 bump 
* Magic Window Demo
* Fix to local style JSON load example 
* Reimplement location examples using component  
* Java/Kotlin toggle, MainActivity refactor
* Use Kotlin-Android-Extensions to remove extra code 
* Location layer plugin 0.10.0 release bump
* Add kotlin circle layer example
* Remove transfuser errata
* Variable rename in style fade switch example
* Docker image update
* Rainfall data example tweaks
* Java SDK 4.0.0 bump 
* Basic Kotlin version of plain map 

## 6.5.0

* Device location in a map fragment
* Transparent background + video example
* Adding line background highlight example
* Data-driven styling temperature expression example

## 6.4.0

* Calendar integration
* Video background

## 6.3.0

* Static image API
* Building outline

## 6.2.1

* GeoJSON SymbolLayer clustering
* Place picker

## 6.2.0

* New example of showing an info window with a SymbolLayer
* New example of water depth (bathymetry) data visualization
* New geocoding example 

## 6.1.0

* Re-added the traffic plugin example
* Added a straightforward ImageSource example
* Other bug fixes

## 6.0.1

* A new hillshading example
* A new heatmap example

## 5.5.1

* Location layer plugin update
* Several small hot fixes
* Mapbox Java SDK upgrade

## 5.1.4

* How to use the new GeoJSON plugin
* How to add many images as markers

## 5.1.3-2

* Rotate and tilt the map based on device rotation
* Example of using the Mapbox Optimization API

## 5.1.2

* Picture-in-picture (for Android O)
* Adaptive icons (for Android O)
* Loading a map style via a local file

## 5.1.0

* Extrusions (3D buildings)
* Data-driven styling
* Annotations
* Hotfixes of several examples

## 4.2.0
* Indoor map example
* Los Angeles tourism example
* Symbol layer example
* Location memory leaks resolved

## 4.2.0-beta.3 - September 21, 2016
* Mapbox Android SDK v4.2.0-beta.3
* Landuse Styling - lab example
* OffRoute example

## 4.2.0-beta.2 - August 25, 2016
* Mapbox Android SDK v4.2.0-beta.2
* Query features examples added

## 4.2.0-beta.1 - August 6, 2016
* Mapbox Android SDK v4.2.0-beta.1
* Runtime style examples added

## 4.1.1
* Mapbox Android SDK v4.1.1
* Mapbox Android Service v1.2.1

## 4.1.0
* Lab category has been added giving a section for more complex code examples
* Location picker example
* Track user location example
* Basic marker view example
* Animate marker example
* Switching to a different example category now scrolls the recycler view back to top
* Back to home doesn't destroy the activity now
* Added missing example images
* Fixed padding between cards
