# SmartHealthTracker ProGuard rules

# Keep Kotlin metadata
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keepattributes Signature
-keepattributes Exceptions

# Keep Room entities
-keep class com.smarthealthtracker.data.entities.** { *; }

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep @dagger.hilt.android.HiltAndroidApp class * { *; }
-keep @dagger.hilt.android.AndroidEntryPoint class * { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }

# Keep ViewModel
-keep class androidx.lifecycle.ViewModel { *; }
-keepclassmembers class * extends androidx.lifecycle.ViewModel { public <init>(...); }

# Gson / serialization (for backup)
-keep class com.google.gson.** { *; }
-keep class com.smarthealthtracker.ui.settings.BackupData { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Navigation safe args
-keep class com.smarthealthtracker.ui.**.*Args { *; }
-keep class com.smarthealthtracker.ui.**.*Directions { *; }

# MPAndroidChart
-keep class com.github.mikephil.charting.** { *; }

# Kizitonwose Calendar
-keep class com.kizitonwose.calendar.** { *; }

# WorkManager
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context,androidx.work.WorkerParameters);
}

# Parcelable
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** { volatile <fields>; }

# General Android
-dontwarn sun.misc.**
-keep class sun.misc.Unsafe { *; }
