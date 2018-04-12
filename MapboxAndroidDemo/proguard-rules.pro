# Retrofit 2
# Platform calls Class.forName on types which do not exist on Android to determine platform.
-dontnote retrofit2.Platform
# Platform used when running on RoboVM on iOS. Will not be used at runtime.
-dontnote retrofit2.Platform$IOS$MainThreadExecutor
# Platform used when running on Java 8 VMs. Will not be used at runtime.
-dontwarn retrofit2.Platform$Java8
# Retain generic type information for use by reflection by converters and adapters.
-keepattributes Signature
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
-dontwarn java.awt.**

-keep class com.segment.analytics.** { *; }
-keep class com.mapbox.mapboxandroiddemo.model.usermodel.** { *; }

# MAS data models that will be serialized/deserialized over Gson
-keep class com.mapbox.services.api.directionsmatrix.v1.models.** { *; }

# --- GMS ---
-keep public class com.google.android.gms.* { public *; }
-dontwarn com.google.android.gms.**


# Consumer proguard rules for plugins

# --- AutoValue ---
# AutoValue annotations are retained but dependency is compileOnly.
-dontwarn com.google.auto.value.**

# --- Retrofit ---
# Retain generic type information for use by reflection by converters and adapters.
-keepattributes Signature
# Retain service method parameters.
-keepclassmembernames,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
# Ignore annotation used for build tooling.
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

# --- OkHttp ---
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
# A resource is loaded with a relative path so the package of this class must be preserved.
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# --- Java ---
-dontwarn java.awt.Color
