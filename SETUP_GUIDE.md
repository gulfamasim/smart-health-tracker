# Smart Health Tracker — Complete Setup & Build Guide

## 📦 Project Summary

| Item | Detail |
|---|---|
| **App Name** | Smart Health Tracker |
| **Package** | `com.smarthealthtracker` |
| **Language** | Kotlin |
| **Min SDK** | API 26 (Android 8.0+) |
| **Target SDK** | API 34 (Android 14) |
| **Architecture** | MVVM + Clean Architecture |
| **DI** | Hilt |
| **Database** | Room |
| **UI** | Material Design 3 |
| **Navigation** | Navigation Component |
| **Background** | WorkManager + AlarmManager |

---

## 🗂 Project Structure

```
SmartHealthTracker/
├── app/
│   ├── build.gradle                        # All dependencies declared here
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/smarthealthtracker/
│       │   ├── SmartHealthApp.kt           # Application class (Hilt + Channels)
│       │   ├── data/
│       │   │   ├── dao/Daos.kt             # All Room DAOs
│       │   │   ├── db/
│       │   │   │   ├── Converters.kt       # Type converters
│       │   │   │   └── HealthDatabase.kt   # Room DB definition
│       │   │   ├── entities/Entities.kt    # All @Entity data classes + enums
│       │   │   └── repository/
│       │   │       └── HealthRepository.kt # Single source of truth
│       │   ├── di/
│       │   │   ├── AppModule.kt            # Hilt module (DB, scheduler)
│       │   │   └── AlarmSchedulerImpl.kt   # AlarmManager wrapper
│       │   ├── receiver/
│       │   │   ├── AlarmReceiver.kt        # Fires on scheduled alarm
│       │   │   ├── BootReceiver.kt         # Reschedules after reboot
│       │   │   └── NotificationActionReceiver.kt  # Complete/Snooze/Dismiss
│       │   ├── service/
│       │   │   ├── AlarmService.kt         # Foreground service for notifications
│       │   │   └── DailyRefreshWorker.kt   # WorkManager daily job
│       │   ├── ui/
│       │   │   ├── MainActivity.kt         # Single-activity host
│       │   │   ├── SplashActivity.kt
│       │   │   ├── dashboard/              # Today's timeline
│       │   │   ├── calendar/               # Monthly calendar view
│       │   │   ├── entries/                # Add/Edit entry
│       │   │   ├── reminders/              # Schedules + chain logic
│       │   │   ├── statistics/             # Charts & streaks
│       │   │   └── settings/               # Dark mode, backup/restore
│       │   └── widget/                     # Home screen widget
│       └── res/
│           ├── anim/           # Slide transitions
│           ├── drawable/       # 17 vector icons + shape drawables
│           ├── layout/         # 14 XML layouts
│           ├── menu/           # Bottom nav menu
│           ├── mipmap-*/       # Adaptive launcher icons
│           ├── navigation/     # nav_graph.xml
│           ├── values/         # Colors, strings, themes (light)
│           ├── values-night/   # Dark theme overrides
│           └── xml/            # Widget info, backup rules
├── gradle/wrapper/
│   └── gradle-wrapper.properties
├── build.gradle
├── settings.gradle
└── gradle.properties
```

---

## 🛠 Prerequisites

Before building, install the following:

### 1. Java Development Kit (JDK 17)
```bash
# macOS (Homebrew)
brew install openjdk@17

# Ubuntu/Debian
sudo apt install openjdk-17-jdk

# Windows: Download from https://adoptium.net
```

### 2. Android Studio (Hedgehog 2023.1.1 or newer)
Download from: https://developer.android.com/studio

Android Studio bundles:
- Android SDK (API 26–34)
- Gradle build tools
- ADB (Android Debug Bridge)
- Emulator

---

## 🏗 Build Steps

### Step 1 — Extract the project
```bash
unzip SmartHealthTracker.zip -d ~/Projects/
cd ~/Projects/SmartHealthTracker
```

### Step 2 — Open in Android Studio
1. Launch **Android Studio**
2. Click **File → Open**
3. Navigate to `~/Projects/SmartHealthTracker` and click **OK**
4. Wait for Gradle sync to complete (first run downloads ~500 MB of dependencies)

### Step 3 — Accept SDK licenses (if prompted)
```bash
# In terminal (optional, Studio usually handles this)
$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager --licenses
```

### Step 4 — Add the MPAndroidChart repository
In `settings.gradle`, ensure `jitpack.io` is in the repositories block:
```groovy
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }   // ← required for MPAndroidChart
    }
}
```

### Step 5 — Build a Debug APK
```bash
# From project root (macOS/Linux)
./gradlew assembleDebug

# Windows
gradlew.bat assembleDebug
```
Output: `app/build/outputs/apk/debug/app-debug.apk`

### Step 6 — Build a Release APK (unsigned, for testing)
```bash
./gradlew assembleRelease
```
Output: `app/build/outputs/apk/release/app-release-unsigned.apk`

---

## 🔑 Signing a Release APK (for distribution)

### Generate a keystore
```bash
keytool -genkey -v \
  -keystore health_tracker.jks \
  -alias health_tracker \
  -keyalg RSA -keysize 2048 \
  -validity 10000
```

### Configure signing in `app/build.gradle`
```groovy
android {
    signingConfigs {
        release {
            storeFile     file("../health_tracker.jks")
            storePassword "your_store_password"
            keyAlias      "health_tracker"
            keyPassword   "your_key_password"
        }
    }
    buildTypes {
        release {
            signingConfig signingConfigs.release
            minifyEnabled true
            shrinkResources true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
}
```

### Build signed release APK
```bash
./gradlew assembleRelease
```
Output: `app/build/outputs/apk/release/app-release.apk`

---

## 📲 Installing the APK

### Method A — ADB (Android Debug Bridge)
```bash
# Enable USB Debugging on your phone:
# Settings → About Phone → tap Build Number 7 times
# Settings → Developer Options → USB Debugging ON

# Connect via USB, then:
adb install app/build/outputs/apk/debug/app-debug.apk

# Or for release:
adb install app/build/outputs/apk/release/app-release.apk
```

### Method B — Direct file transfer
1. Copy the APK to your phone via USB, Google Drive, WhatsApp, etc.
2. On your phone: **Settings → Security → Allow Unknown Sources** (or "Install unknown apps")
3. Open a file manager, navigate to the APK, and tap to install

### Method C — Android Studio (easiest for development)
1. Connect your phone via USB
2. In Android Studio: **Run → Run 'app'**
3. Select your device and click **OK**

---

## ✅ First-Run Permissions

The app will request the following permissions on first launch:

| Permission | Purpose | Required? |
|---|---|---|
| POST_NOTIFICATIONS | Show meal/medicine reminders | Yes |
| SCHEDULE_EXACT_ALARM | Precise alarm timing | Yes (Settings redirect) |
| RECEIVE_BOOT_COMPLETED | Restore alarms after reboot | Auto-granted |
| VIBRATE | Vibration on alarm | Auto-granted |
| READ/WRITE_EXTERNAL_STORAGE | Backup/restore (Android ≤ 12) | For backup only |

---

## 🧪 Testing on Emulator

1. In Android Studio → **Tools → Device Manager → Create Device**
2. Choose: Pixel 6 (or any API 26+)
3. System Image: **API 34 (Google Play)**
4. Click Finish, then Run

> **Note:** Exact alarms on emulator may behave differently. Test on a physical device for full alarm reliability.

---

## 🔧 Troubleshooting

### Gradle sync fails — "Could not resolve dependency"
```bash
# Clear Gradle cache and re-sync
./gradlew clean
rm -rf ~/.gradle/caches/modules-2/files-2.1/com.github.PhilJay/
# Then sync again in Android Studio
```

### "Cannot find symbol: R.id.XXX"
- Run **Build → Clean Project**, then **Build → Rebuild Project**

### Notifications not firing on Xiaomi/MIUI, Huawei/EMUI, OnePlus, Samsung
These OEMs aggressively kill background processes. Steps to fix:
- **Xiaomi**: Settings → Apps → Manage Apps → Smart Health Tracker → Battery Saver → No restrictions
- **Huawei**: Settings → Battery → App Launch → Smart Health Tracker → Manage manually → Allow all
- **Samsung**: Settings → Device Care → Battery → Background Usage Limits → Disable for app

### SCHEDULE_EXACT_ALARM permission (Android 12+)
If alarms are not firing, the user may not have granted exact alarm permission:
- **Settings → Apps → Special app access → Alarms & reminders → Smart Health Tracker → Allow**

### Room database migration error
If you modify entities, bump the database `version` in `HealthDatabase.kt` and add a Migration:
```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE health_entries ADD COLUMN new_column TEXT DEFAULT ''")
    }
}
// In DatabaseModule:
Room.databaseBuilder(...)
    .addMigrations(MIGRATION_1_2)
    .build()
```

---

## 🗺 Architecture Overview

```
UI Layer (Fragments + ViewModels)
        ↓ StateFlow / SharedFlow
Repository Layer (HealthRepository)
        ↓ suspend functions + Flow
DAO Layer (Room DAOs)
        ↓ SQL queries
Room Database (SQLite)

Alarm System:
  AlarmManager ──triggers──► AlarmReceiver
                              ↓
                         AlarmService (foreground)
                              ↓
                    Notification + Vibration

Chain Logic:
  User marks Complete
        ↓ NotificationActionReceiver
  completeEntry() → returns next ReminderSchedule
        ↓ if chainDelayMinutes > 0
  AlarmScheduler.scheduleChainAlarm()
        ↓ (delay minutes later)
  Next alarm fires automatically
```

---

## ⭐ Feature Checklist

| Feature | Status |
|---|---|
| Create/Edit/Delete meal entries | ✅ |
| Create/Edit/Delete medicine entries | ✅ |
| Daily timeline dashboard | ✅ |
| Custom reminder schedules | ✅ |
| Per-day time overrides (Mon–Sun) | ✅ |
| Chain reminder logic | ✅ |
| Mark Eaten / Mark Taken | ✅ |
| Daily completion history | ✅ |
| Missed vs completed tracking | ✅ |
| Sound + vibration alarm | ✅ |
| Persistent notification until marked done | ✅ |
| Snooze (10 min) | ✅ |
| Material Design 3 | ✅ |
| Dashboard with progress ring | ✅ |
| Calendar view with color dots | ✅ |
| Add/Edit screens | ✅ |
| Streak tracking | ✅ |
| Bar + Pie charts (MPAndroidChart) | ✅ |
| Dark mode support | ✅ |
| Room database (offline-first) | ✅ |
| Boot receiver (alarm persistence) | ✅ |
| WorkManager daily refresh job | ✅ |
| JSON backup/restore | ✅ |
| Home screen widget | ✅ |
| Hilt dependency injection | ✅ |
| ProGuard / R8 minification | ✅ |

---

## 📝 Dependencies Reference

```
Material Design 3         → com.google.android.material:material:1.11.0
Room Database             → androidx.room:room-runtime:2.6.1
Hilt DI                   → com.google.dagger:hilt-android:2.50
Navigation Component      → androidx.navigation:navigation-fragment-ktx:2.7.6
WorkManager               → androidx.work:work-runtime-ktx:2.9.0
Kizitonwose Calendar      → com.kizitonwose.calendar:view:2.4.0
MPAndroidChart            → com.github.PhilJay:MPAndroidChart:v3.1.0
Gson                      → com.google.code.gson:gson:2.10.1
DataStore Preferences     → androidx.datastore:datastore-preferences:1.0.0
Coroutines                → org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3
ViewModel + LiveData      → androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0
KSP                       → com.google.devtools.ksp:1.9.22-1.0.17
```

---

## 🤝 Contributing / Extending

### Add a new screen
1. Create `Fragment` + `ViewModel` in appropriate `ui/` subpackage
2. Add `<fragment>` entry to `res/navigation/nav_graph.xml`
3. Add `<item>` to `res/menu/bottom_nav_menu.xml` (if top-level)

### Add a new entity field
1. Update `data/entities/Entities.kt`
2. Update relevant DAO queries in `data/dao/Daos.kt`
3. Update `HealthRepository` if needed
4. Bump Room database version and add Migration

### Voice reminders (bonus extension)
Use Android's `TextToSpeech` API inside `AlarmService`:
```kotlin
val tts = TextToSpeech(context) { status ->
    if (status == TextToSpeech.SUCCESS) {
        tts.speak("Time for your $title", TextToSpeech.QUEUE_FLUSH, null, null)
    }
}
```

---

*Smart Health Tracker v1.0.0 — Built with Kotlin + Jetpack + Material 3*
