# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
-renamesourcefileattribute SourceFile

-dontobfuscate

# Disable ServiceLoader reproducibility-breaking optimizations
-keep class kotlinx.coroutines.CoroutineExceptionHandler
-keep class kotlinx.coroutines.internal.MainDispatcherFactory

# ksoap2 XmlPullParser confusion
-dontwarn org.xmlpull.v1.XmlPullParser
-dontwarn org.xmlpull.v1.XmlSerializer
-keep class org.xmlpull.v1.* {*;}

# Rome
-keep class com.rometools.** { *; }

# Provider API
-keep class me.ash.reader.** { *; }

# https://github.com/flutter/flutter/issues/127388
-dontwarn org.kxml2.io.KXml**

# https://youtrack.jetbrains.com/issue/KTOR-5528
-dontwarn org.slf4j.impl.StaticLoggerBinder

-keep class me.ash.reader.R$font { *; }