# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\user\AppData\Local\Android\Sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# App - Some methods are only called from tests, so make sure the shrinker keeps them.
-keep class com.lupinemoon.favicoin.** { *; }

# Calligraphy
-keep class io.github.inflationx.calligraphy3.* { *; }
-keep class io.github.inflationx.calligraphy3.*$* { *; }

# Databinding
-dontnote android.databinding.**

# Fabric
-keep class com.crashlytics.** { *; }
-dontwarn com.crashlytics.**
-dontnote io.fabric.**

# Glide
-keep class com.bumptech.glide.integration.okhttp3.OkHttpGlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}
-dontnote com.bumptech.**

# GSON
-dontnote com.google.gson.**

# Leak Canary
-dontwarn com.squareup.haha.guava.**
-dontwarn com.squareup.haha.perflib.**
-dontwarn com.squareup.haha.trove.**
-dontwarn com.squareup.leakcanary.**
-keep class com.squareup.haha.** { *; }
-keep class com.squareup.leakcanary.** { *; }

# Marshmallow removed Notification.setLatestEventInfo()
-dontwarn android.app.Notification

# OkHttp
-keepattributes Signature
-keepattributes *Annotation*
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**

# Okio
-keep class sun.misc.Unsafe { *; }
-dontwarn java.nio.file.*
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn okio.**
-dontnote okio.**

# Realm
-dontnote io.realm.**

# Retrofit
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions

-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# RxJava2
-dontnote io.reactivex.**

# Timber
-dontwarn org.jetbrains.annotations.**

# Parceler library
-keep interface org.parceler.Parcel
-keep @org.parceler.Parcel class * { *; }
-keep class **$$Parcelable { *; }
-dontnote org.parceler.**

# RxJava2 Extras
-dontwarn sun.misc.**
-dontwarn com.github.davidmoten.rx2.flowable.Serialized*
-dontwarn com.github.davidmoten.rx2.internal.flowable.buffertofile.MemoryMappedFile
-dontwarn com.github.davidmoten.rx2.internal.flowable.buffertofile.UnsafeAccess






