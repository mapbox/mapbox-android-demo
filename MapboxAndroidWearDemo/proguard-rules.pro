# Retrofit 2
# Platform calls Class.forName on types which do not exist on Android to determine platform.
-dontnote retrofit2.Platform
# Platform used when running on RoboVM on iOS. Will not be used at runtime.
-dontnote retrofit2.Platform$IOS$MainThreadExecutor
# Platform used when running on Java 8 VMs. Will not be used at runtime.
-dontwarn retrofit2.Platform$Java8
# Retain generic type information for use by reflection by converters and adapters.
-keepattributes *Annotation*,Signature

# Retain declared checked exceptions for use by a Proxy instance.
-keepattributes Exceptions

# For using GSON @Expose annotation
-keepattributes *Annotation*
# Gson specific classes
-dontwarn sun.misc.**

-dontwarn okio.**
-dontwarn okhttp3.**
-keep class retrofit.**
-keep class retrofit.** { *; }
-keepclasseswithmembers class * {
    @retrofit.http.* <methods>;
}
-dontwarn retrofit.**

# Picasso
-dontwarn com.squareup.okhttp.**

-dontwarn android.support.**
-dontwarn java.lang.**
-dontwarn org.codehaus.**
-dontwarn com.google.**
-dontwarn java.nio.**
-dontwarn javax.annotation.**

-keep class com.segment.analytics.** { *; }


-dontwarn com.cunoraz.gifview.library.**

# --- GMS ---
-keep public class com.google.android.gms.* { public *; }
-dontwarn com.google.android.gms.**

# Consumer proguard rules for plugins

# --- AutoValue ---
# AutoValue annotations are retained but dependency is compileOnly.
-dontwarn com.google.auto.value.**

# --- Retrofit ---
# Retain service method parameters.
-keepclassmembernames interface * {
    @retrofit2.http.* <methods>;
}
# Ignore annotation used for build tooling.
-dontwarn org.codehaus.mojo.animal_sniffer.*

# GSON
-keep class com.google.gson.** { *; }


# Consumer proguard rules for plugins

-dontwarn com.mapbox.mapboxandroiddemo.examples.plugins.**
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

# MAS data models that will be serialized/deserialized over Gson
-keep class com.mapbox.services.api.directionsmatrix.v1.models.** { *; }

-keep class android.arch.lifecycle.** { *; }
-keep class com.mapbox.android.core.location.** { *; }
-keep class com.mapbox.mapboxsdk.** { *; }

-dontnote com.squareup.**

# --- OkHttp ---
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn okio.BufferedSink
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
# A resource is loaded with a relative path so the package of this class must be preserved.
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

-dontwarn okhttp3.internal.platform.ConscryptPlatform

# --- Java ---
-dontwarn java.awt.Color
-dontwarn com.mapbox.api.staticmap.v1.models.StaticMarkerAnnotation
-dontwarn com.mapbox.api.staticmap.v1.models.StaticPolylineAnnotation

# Mapbox
-keep class com.mapbox.android.telemetry.**
-keep class com.mapbox.android.core.location.**
-keep class android.arch.lifecycle.** { *; }
-keep class com.mapbox.android.core.location.** { *; }
-keep class com.mapbox.mapboxsdk.** { *; }

# Other Android
-keep public class com.google.firebase.** { public *; }
-keep class com.google.firebase.** { *; }
-dontnote android.net.http.*
-dontnote org.apache.commons.codec.**
-dontnote org.apache.http.**
-dontwarn org.xmlpull.v1.**
-dontnote org.xmlpull.v1.**
-keep class org.xmlpull.** { *; }
-keepclassmembers class org.xmlpull.** { *; }