package com.smarthealthtracker

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.smarthealthtracker.service.DailyRefreshWorker
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Application class — initialises Hilt DI, notification channels, and WorkManager.
 */
@HiltAndroidApp
class SmartHealthApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()

    override fun onCreate() {
        super.onCreate()
        applyThemePreference()
        createNotificationChannels()
        DailyRefreshWorker.schedule(this)
    }

    /** Restore the user's dark/light mode preference on every app launch. */
    private fun applyThemePreference() {
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val isDark = prefs.getBoolean("dark_mode", true)
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
            if (isDark) androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
            else androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)

            val alarmChannel = NotificationChannel(
                CHANNEL_ALARM, "Health Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Meal and medication reminders"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 500)
                setShowBadge(true)
                enableLights(true)
                lightColor = android.graphics.Color.parseColor("#4CAF50")
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }

            val progressChannel = NotificationChannel(
                CHANNEL_PROGRESS, "Daily Progress",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Ongoing daily health progress"
                setShowBadge(false)
            }

            val infoChannel = NotificationChannel(
                CHANNEL_INFO, "Health Insights",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Health tips, streaks, and suggestions"
                setShowBadge(true)
            }

            nm.createNotificationChannels(listOf(alarmChannel, progressChannel, infoChannel))
        }
    }

    companion object {
        const val CHANNEL_ALARM    = "channel_alarm"
        const val CHANNEL_PROGRESS = "channel_progress"
        const val CHANNEL_INFO     = "channel_info"
    }
}
