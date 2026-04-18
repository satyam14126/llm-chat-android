# Add project specific ProGuard rules here.
# By default, the flags in this file are applied to all builds.

# Retrofit
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }

# OkHttp
-dontwarn okhttp3.**
-keep class okhttp3.** { *; }
-dontwarn okio.**

# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.** { kotlinx.serialization.KSerializer serializer(...); }

# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.**

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# App models
-keep class com.llmchat.app.data.remote.dto.** { *; }
-keep class com.llmchat.app.util.ChatExport { *; }
-keep class com.llmchat.app.util.SessionExport { *; }
-keep class com.llmchat.app.util.MessageExport { *; }
