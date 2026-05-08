package com.smarthealthtracker.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.smarthealthtracker.data.entities.DayOfWeek
import com.smarthealthtracker.data.repository.HealthRepository
import com.smarthealthtracker.di.AlarmScheduler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * Reschedules all active alarms after device reboot.
 * Android cancels all alarms on reboot — this receiver restores them.
 */
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject lateinit var repository: HealthRepository
    @Inject lateinit var alarmScheduler: AlarmScheduler

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != "android.intent.action.QUICKBOOT_POWERON") return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                rescheduleAlarms()
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun rescheduleAlarms() {
        val schedules = repository.getAllSchedulesOnce()
        val today = LocalDate.now()
        val todayDow = today.dayOfWeek.name.take(3).let {
            DayOfWeek.valueOf(it.uppercase())
        }
        val now = LocalTime.now()
        val formatter = DateTimeFormatter.ofPattern("HH:mm")

        for (schedule in schedules) {
            if (!schedule.isEnabled) continue
            if (todayDow !in schedule.activeDays) continue

            val timeStr = schedule.dayTimeOverrides[todayDow.name] ?: schedule.defaultTime
            val scheduledTime = LocalTime.parse(timeStr, formatter)

            // Only schedule if the time hasn't passed yet today
            if (scheduledTime.isAfter(now)) {
                val triggerMs = today.atTime(scheduledTime)
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()

                // Use schedule ID as a proxy entry ID for boot rescheduling
                alarmScheduler.scheduleAlarm(
                    entryId = schedule.id,
                    triggerTimeMs = triggerMs,
                    title = schedule.title,
                    category = schedule.category.name
                )
            }
        }
    }
}
