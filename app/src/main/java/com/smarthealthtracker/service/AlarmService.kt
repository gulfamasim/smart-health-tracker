package com.smarthealthtracker.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.smarthealthtracker.R
import com.smarthealthtracker.SmartHealthApp
import com.smarthealthtracker.receiver.AlarmReceiver
import com.smarthealthtracker.receiver.NotificationActionReceiver
import com.smarthealthtracker.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint

/**
 * Foreground service that fires when an alarm triggers.
 * Displays a persistent, actionable notification with sound and vibration.
 * Stays active until user taps Complete, Snooze, or Dismiss.
 */
@AndroidEntryPoint
class AlarmService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val entryId = intent?.getLongExtra(AlarmReceiver.EXTRA_ENTRY_ID, -1L) ?: -1L
        val title   = intent?.getStringExtra(AlarmReceiver.EXTRA_TITLE) ?: "Health Reminder"
        val category = intent?.getStringExtra(AlarmReceiver.EXTRA_CATEGORY) ?: "FOOD"

        val notification = buildNotification(entryId, title, category)
        startForeground(entryId.toInt().coerceAtLeast(1), notification)
        vibrateDevice()

        // Stop foreground immediately after posting notification
        // (notification stays visible until user acts on it)
        stopForeground(STOP_FOREGROUND_DETACH)
        stopSelf(startId)

        return START_NOT_STICKY
    }

    private fun buildNotification(entryId: Long, title: String, category: String): Notification {
        val isMedicine = category == "MEDICINE"

        val icon = if (isMedicine) R.drawable.ic_medicine else R.drawable.ic_food
        val emoji = if (isMedicine) "💊" else "🍽️"
        val subtext = if (isMedicine) "Time to take your medicine" else "Time for your meal"
        val completeLabel = if (isMedicine) "Taken ✓" else "Eaten ✓"

        // Open app on tap
        val openAppIntent = PendingIntent.getActivity(
            this, entryId.toInt(),
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("entry_id", entryId)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Complete action
        val completeIntent = PendingIntent.getBroadcast(
            this, (entryId * 10 + 1).toInt(),
            Intent(this, NotificationActionReceiver::class.java).apply {
                action = NotificationActionReceiver.ACTION_COMPLETE
                putExtra(NotificationActionReceiver.EXTRA_ENTRY_ID, entryId)
                putExtra(NotificationActionReceiver.EXTRA_TITLE, title)
                putExtra(NotificationActionReceiver.EXTRA_CATEGORY, category)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Snooze action
        val snoozeIntent = PendingIntent.getBroadcast(
            this, (entryId * 10 + 2).toInt(),
            Intent(this, NotificationActionReceiver::class.java).apply {
                action = NotificationActionReceiver.ACTION_SNOOZE
                putExtra(NotificationActionReceiver.EXTRA_ENTRY_ID, entryId)
                putExtra(NotificationActionReceiver.EXTRA_TITLE, title)
                putExtra(NotificationActionReceiver.EXTRA_CATEGORY, category)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Dismiss action
        val dismissIntent = PendingIntent.getBroadcast(
            this, (entryId * 10 + 3).toInt(),
            Intent(this, NotificationActionReceiver::class.java).apply {
                action = NotificationActionReceiver.ACTION_DISMISS
                putExtra(NotificationActionReceiver.EXTRA_ENTRY_ID, entryId)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        return NotificationCompat.Builder(this, SmartHealthApp.CHANNEL_ALARM)
            .setSmallIcon(icon)
            .setContentTitle("$emoji $title")
            .setContentText(subtext)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(subtext)
                .setSummaryText("Smart Health Tracker"))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSound(alarmSound)
            .setOngoing(true)               // persistent until user acts
            .setAutoCancel(false)
            .setContentIntent(openAppIntent)
            .addAction(R.drawable.ic_check, completeLabel, completeIntent)
            .addAction(R.drawable.ic_snooze, "Snooze 10m", snoozeIntent)
            .addAction(R.drawable.ic_dismiss, "Dismiss", dismissIntent)
            .build()
    }

    private fun vibrateDevice() {
        val pattern = longArrayOf(0, 500, 200, 500, 200, 500)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = getSystemService(VibratorManager::class.java)
            vm?.defaultVibrator?.vibrate(
                VibrationEffect.createWaveform(pattern, -1)
            )
        } else {
            @Suppress("DEPRECATION")
            val v = getSystemService(VIBRATOR_SERVICE) as? Vibrator
            v?.vibrate(VibrationEffect.createWaveform(pattern, -1))
        }
    }
}
