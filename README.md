# 🏥 Smart Health Tracker

A feature-rich Android app for tracking meals and medications with smart reminders, streak tracking, and detailed analytics — built with modern Android architecture.

![Android](https://img.shields.io/badge/Android-API%2026%2B-brightgreen?logo=android)
![Kotlin](https://img.shields.io/badge/Kotlin-1.9.22-blueviolet?logo=kotlin)
![Material Design 3](https://img.shields.io/badge/Material%20Design-3-blue?logo=material-design)
![Architecture](https://img.shields.io/badge/Architecture-MVVM%20%2B%20Clean-orange)
![License](https://img.shields.io/badge/License-MIT-lightgrey)

---

## ✨ Features

- 🍽️ **Meal & Medicine Tracking** — Create, edit, and delete entries with custom reminder schedules
- ⏰ **Smart Reminders** — Precise alarms with sound, vibration, snooze (10 min), and persistent notifications
- 🔗 **Chain Reminder Logic** — Automatically schedule follow-up reminders after marking an entry complete
- 📅 **Daily Timeline Dashboard** — Visual progress ring showing today's completion status
- 🗓️ **Calendar View** — Monthly overview with color-coded dots for completed/missed days
- 📊 **Statistics & Streaks** — Bar and pie charts powered by MPAndroidChart, plus streak tracking
- 🌙 **Dark Mode** — Full Material Design 3 theming with light/dark support
- 🔁 **Boot Persistence** — Alarms automatically restored after device reboot
- 💾 **Offline-First** — Room database with JSON backup & restore
- 🖼️ **Home Screen Widget** — Quick-glance widget for today's status
- 🗓️ **Per-Day Overrides** — Set different reminder times for each day of the week (Mon–Sun)

---

## 📸 Screenshots

> <img width="1080" height="2400" alt="Screenshot 9" src="https://github.com/user-attachments/assets/14b3277a-b097-41aa-b88b-53631921de9f" />

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Material Design 3, XML Layouts |
| Architecture | MVVM + Clean Architecture |
| Dependency Injection | Hilt 2.50 |
| Database | Room 2.6.1 |
| Navigation | Navigation Component 2.7.6 |
| Background Tasks | WorkManager 2.9.0 + AlarmManager |
| Charts | MPAndroidChart v3.1.0 |
| Calendar | Kizitonwose Calendar 2.4.0 |
| Serialization | Gson 2.10.1 |
| Preferences | DataStore 1.0.0 |
| Async | Kotlin Coroutines 1.7.3 |
| Min SDK | API 26 (Android 8.0) |
| Target SDK | API 34 (Android 14) |

---

## 🚀 Getting Started

### Prerequisites

- **JDK 17** — [Download from Adoptium](https://adoptium.net)
- **Android Studio Hedgehog (2023.1.1)** or newer — [Download here](https://developer.android.com/studio)

### Build Instructions

```bash
# 1. Clone the repository
git clone https://github.com/your-username/SmartHealthTracker.git
cd SmartHealthTracker

# 2. Create drawable resources
bash create_drawables.sh

# 3. Build debug APK
./gradlew assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk

# 4. (Optional) Build release APK
./gradlew assembleRelease
```

> **Note:** The first Gradle sync downloads ~500 MB of dependencies. Make sure `jitpack.io` is present in your `settings.gradle` repositories block (required for MPAndroidChart).

### Install on Device

```bash
# Via ADB (USB Debugging must be enabled)
adb install app/build/outputs/apk/debug/app-debug.apk
```

Or use **Run → Run 'app'** directly from Android Studio.

---

## 📁 Project Structure

```
SmartHealthTracker/
├── app/src/main/java/com/smarthealthtracker/
│   ├── SmartHealthApp.kt           # Application class (Hilt + Channels)
│   ├── data/
│   │   ├── dao/Daos.kt             # All Room DAOs
│   │   ├── db/HealthDatabase.kt    # Room DB definition
│   │   ├── entities/Entities.kt    # @Entity data classes + enums
│   │   └── repository/HealthRepository.kt
│   ├── di/                         # Hilt modules
│   ├── receiver/                   # AlarmReceiver, BootReceiver, ActionReceiver
│   ├── service/                    # AlarmService (foreground), DailyRefreshWorker
│   ├── ui/
│   │   ├── dashboard/              # Today's timeline
│   │   ├── calendar/               # Monthly calendar view
│   │   ├── entries/                # Add/Edit entry screens
│   │   ├── reminders/              # Schedules + chain logic
│   │   ├── statistics/             # Charts & streaks
│   │   └── settings/               # Dark mode, backup/restore
│   └── widget/                     # Home screen widget
└── app/src/main/res/
    ├── drawable/                   # 17 vector icons
    ├── layout/                     # 14 XML layouts
    ├── navigation/nav_graph.xml
    ├── values/                     # Colors, strings, themes
    └── values-night/               # Dark theme overrides
```

---

## ⚙️ Architecture

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
```

---

## 🔐 Permissions

| Permission | Purpose |
|---|---|
| `POST_NOTIFICATIONS` | Show meal/medicine reminders |
| `SCHEDULE_EXACT_ALARM` | Precise alarm timing |
| `RECEIVE_BOOT_COMPLETED` | Restore alarms after reboot |
| `VIBRATE` | Vibration on alarm |
| `READ/WRITE_EXTERNAL_STORAGE` | Backup/restore (Android ≤ 12 only) |

---

## 🐛 Known Issues & Troubleshooting

**Notifications not firing on Xiaomi / Huawei / Samsung?**
These OEMs aggressively restrict background processes. Go to **Settings → Apps → Smart Health Tracker → Battery** and set it to **No Restrictions**.

**Exact alarms not working on Android 12+?**
Grant permission via **Settings → Apps → Special app access → Alarms & reminders → Smart Health Tracker → Allow**.

**Gradle sync failing?**
```bash
./gradlew clean
rm -rf ~/.gradle/caches/modules-2/files-2.1/com.github.PhilJay/
```
Then re-sync in Android Studio.

For a full troubleshooting guide, see [SETUP_GUIDE.md](SETUP_GUIDE.md).

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/your-feature`
3. Commit your changes: `git commit -m 'Add your feature'`
4. Push to the branch: `git push origin feature/your-feature`
5. Open a Pull Request

When adding new screens, create a `Fragment` + `ViewModel` in the appropriate `ui/` subpackage, register in `nav_graph.xml`, and (if top-level) add to `bottom_nav_menu.xml`. When modifying entities, bump the Room database version and add a `Migration`.

---

## 📄 License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

---

*Smart Health Tracker v1.0.0 — Built with Kotlin + Jetpack + Material Design 3*
