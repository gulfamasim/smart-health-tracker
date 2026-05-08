package com.smarthealthtracker.di

/**
 * Abstraction over Android AlarmManager for scheduling health reminders.
 */
interface AlarmScheduler {
    fun scheduleAlarm(entryId: Long, triggerTimeMs: Long, title: String, category: String)
    fun cancelAlarm(entryId: Long)
    fun scheduleChainAlarm(entryId: Long, delayMinutes: Int, title: String, category: String)
}
