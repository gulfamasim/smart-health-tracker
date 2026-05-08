package com.smarthealthtracker.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.smarthealthtracker.data.repository.HealthRepository
import com.smarthealthtracker.di.AlarmScheduler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Handles notification action button taps:
 *  • Complete  → marks entry done, triggers chain alarm if configured
 *  • Snooze    → reschedules alarm +10 minutes, updates status to SNOOZED
 *  • Dismiss   → cancels notification only (entry stays PENDING)
 */
@AndroidEntryPoint
class NotificationActionReceiver : BroadcastReceiver() {

    @Inject lateinit var repository: HealthRepository
    @Inject lateinit var alarmScheduler: AlarmScheduler

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        val entryId = intent.getLongExtra(EXTRA_ENTRY_ID, -1L)
        if (entryId == -1L) return

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        when (intent.action) {
            ACTION_COMPLETE -> scope.launch {
                val nextSchedule = repository.completeEntry(entryId)
                notificationManager.cancel(entryId.toInt())

                // Chain alarm: schedule next reminder after configured delay
                if (nextSchedule != null && nextSchedule.chainDelayMinutes > 0) {
                    alarmScheduler.scheduleChainAlarm(
                        entryId = nextSchedule.id * 10000 + System.currentTimeMillis() % 10000,
                        delayMinutes = nextSchedule.chainDelayMinutes,
                        title = nextSchedule.title,
                        category = nextSchedule.category.name
                    )
                }
            }

            ACTION_SNOOZE -> scope.launch {
                repository.snoozeEntry(entryId)
                notificationManager.cancel(entryId.toInt())
                // Reschedule +10 minutes
                alarmScheduler.scheduleChainAlarm(
                    entryId = entryId,
                    delayMinutes = SNOOZE_MINUTES,
                    title = intent.getStringExtra(EXTRA_TITLE) ?: "Reminder",
                    category = intent.getStringExtra(EXTRA_CATEGORY) ?: "FOOD"
                )
            }

            ACTION_DISMISS -> {
                notificationManager.cancel(entryId.toInt())
            }
        }
    }

    companion object {
        const val ACTION_COMPLETE = "com.smarthealthtracker.ACTION_COMPLETE"
        const val ACTION_SNOOZE   = "com.smarthealthtracker.ACTION_SNOOZE"
        const val ACTION_DISMISS  = "com.smarthealthtracker.ACTION_DISMISS"
        const val EXTRA_ENTRY_ID  = "extra_entry_id"
        const val EXTRA_TITLE     = "extra_title"
        const val EXTRA_CATEGORY  = "extra_category"
        const val SNOOZE_MINUTES  = 10
    }
}
