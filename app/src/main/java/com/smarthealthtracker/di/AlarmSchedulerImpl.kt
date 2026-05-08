package com.smarthealthtracker.di

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.smarthealthtracker.receiver.AlarmReceiver
import javax.inject.Inject

/**
 * Concrete AlarmScheduler using Android AlarmManager.
 * Uses setExactAndAllowWhileIdle for reliable delivery even in Doze mode.
 */
class AlarmSchedulerImpl @Inject constructor(
    private val context: Context
) : AlarmScheduler {

    private val alarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun scheduleAlarm(
        entryId: Long, triggerTimeMs: Long, title: String, category: String
    ) {
        val pi = buildPendingIntent(entryId, title, category)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, triggerTimeMs, pi)
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, triggerTimeMs, pi)
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, triggerTimeMs, pi)
        }
    }

    override fun cancelAlarm(entryId: Long) {
        val pi = PendingIntent.getBroadcast(
            context, entryId.toInt(),
            Intent(context, AlarmReceiver::class.java),
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        ) ?: return
        alarmManager.cancel(pi)
        pi.cancel()
    }

    override fun scheduleChainAlarm(
        entryId: Long, delayMinutes: Int, title: String, category: String
    ) {
        val triggerMs = System.currentTimeMillis() + delayMinutes * 60_000L
        scheduleAlarm(entryId, triggerMs, title, category)
    }

    private fun buildPendingIntent(
        entryId: Long, title: String, category: String
    ): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "com.smarthealthtracker.ALARM_TRIGGER"
            putExtra(AlarmReceiver.EXTRA_ENTRY_ID, entryId)
            putExtra(AlarmReceiver.EXTRA_TITLE, title)
            putExtra(AlarmReceiver.EXTRA_CATEGORY, category)
        }
        return PendingIntent.getBroadcast(
            context, entryId.toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
