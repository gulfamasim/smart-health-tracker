package com.smarthealthtracker.data.repository

import com.smarthealthtracker.data.dao.DailyHistoryDao
import com.smarthealthtracker.data.dao.HealthEntryDao
import com.smarthealthtracker.data.dao.ReminderScheduleDao
import com.smarthealthtracker.data.dao.UserPreferenceDao
import com.smarthealthtracker.data.entities.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single source of truth for all health data.
 * Exposes Flows for reactive UI and suspend functions for one-shot operations.
 */
@Singleton
class HealthRepository @Inject constructor(
    private val entryDao: HealthEntryDao,
    private val scheduleDao: ReminderScheduleDao,
    private val historyDao: DailyHistoryDao,
    private val prefDao: UserPreferenceDao
) {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    // ── Entries ──────────────────────────────────────────────────────────────

    fun getEntriesForDate(date: String): Flow<List<HealthEntry>> =
        entryDao.getEntriesForDate(date)

    fun getTodayEntries(): Flow<List<HealthEntry>> =
        entryDao.getEntriesForDate(today())

    fun getEntriesInRange(start: String, end: String): Flow<List<HealthEntry>> =
        entryDao.getEntriesInRange(start, end)

    suspend fun getEntryById(id: Long): HealthEntry? = entryDao.getEntryById(id)

    suspend fun insertEntry(entry: HealthEntry): Long = entryDao.insertEntry(entry)

    suspend fun getEntryByScheduleAndDate(scheduleId: Long, date: String): HealthEntry? =
        entryDao.getEntryByScheduleAndDate(scheduleId, date)

    suspend fun updateEntry(entry: HealthEntry) {
        entryDao.updateEntry(entry)
        refreshDailyHistory(entry.date)
    }

    suspend fun deleteEntry(entry: HealthEntry) {
        entryDao.deleteEntry(entry)
        refreshDailyHistory(entry.date)
    }

    /**
     * Mark entry as completed and trigger chain logic.
     * Returns the next scheduled entry (if chain is configured) or null.
     */
    suspend fun completeEntry(id: Long): ReminderSchedule? {
        val entry = entryDao.getEntryById(id) ?: return null
        entryDao.updateEntryStatus(id, EntryStatus.COMPLETED, System.currentTimeMillis())
        refreshDailyHistory(entry.date)

        // Return chain-linked schedule if configured
        val schedule = entry.scheduleId?.let { scheduleDao.getScheduleById(it) } ?: return null
        val nextId = schedule.chainNextScheduleId ?: return null
        return scheduleDao.getScheduleById(nextId)
    }

    suspend fun snoozeEntry(id: Long) {
        entryDao.updateEntryStatus(id, EntryStatus.SNOOZED, null)
    }

    suspend fun markMissedEntriesBefore(cutoffTime: String, date: String) {
        // Called by background worker — handled via DailyRefreshWorker
    }

    // ── Schedules ────────────────────────────────────────────────────────────

    fun getAllActiveSchedules(): Flow<List<ReminderSchedule>> =
        scheduleDao.getAllActiveSchedules()

    fun getAllSchedules(): Flow<List<ReminderSchedule>> =
        scheduleDao.getAllSchedules()

    fun getSchedulesByCategory(category: Category): Flow<List<ReminderSchedule>> =
        scheduleDao.getSchedulesByCategory(category)

    suspend fun getScheduleById(id: Long): ReminderSchedule? =
        scheduleDao.getScheduleById(id)

    suspend fun insertSchedule(schedule: ReminderSchedule): Long =
        scheduleDao.insertSchedule(schedule)

    suspend fun updateSchedule(schedule: ReminderSchedule) =
        scheduleDao.updateSchedule(schedule)

    suspend fun deleteSchedule(schedule: ReminderSchedule) =
        scheduleDao.deleteSchedule(schedule)

    suspend fun setScheduleEnabled(id: Long, enabled: Boolean) =
        scheduleDao.setEnabled(id, enabled)

    suspend fun getAllSchedulesOnce(): List<ReminderSchedule> =
        scheduleDao.getAllSchedulesOnce()

    /**
     * Generate today's HealthEntry instances from all active schedules.
     * Only inserts entries that don't already exist for today.
     * Skips entries whose scheduled time has already passed.
     */
    suspend fun generateTodayEntries() {
        val today = today()
        val nowTime = java.time.LocalTime.now()
        val todayDow = LocalDate.now().dayOfWeek.name.take(3).let {
            DayOfWeek.valueOf(it.uppercase())
        }

        val schedules = scheduleDao.getAllSchedulesOnce()
        val newEntries = mutableListOf<HealthEntry>()

        for (schedule in schedules) {
            if (!schedule.isEnabled) continue
            if (todayDow !in schedule.activeDays) continue

            // Check if entry already exists for today
            val existing = entryDao.getEntryByScheduleAndDate(schedule.id, today)
            if (existing != null) continue

            // Determine time for today (use override if available)
            val time = schedule.dayTimeOverrides[todayDow.name] ?: schedule.defaultTime

            // Skip if time has already passed
            val timeParts = time.split(":")
            if (timeParts.size == 2) {
                val scheduledLocalTime = runCatching {
                    java.time.LocalTime.of(timeParts[0].toInt(), timeParts[1].toInt())
                }.getOrNull()
                if (scheduledLocalTime != null && scheduledLocalTime.isBefore(nowTime)) continue
            }

            newEntries.add(
                HealthEntry(
                    title = schedule.title,
                    notes = schedule.notes,
                    scheduledTime = time,
                    date = today,
                    category = schedule.category,
                    mealType = schedule.mealType,
                    dosage = schedule.dosage,
                    scheduleId = schedule.id
                )
            )
        }

        if (newEntries.isNotEmpty()) {
            entryDao.insertEntries(newEntries)
        }
    }

    // ── Daily History ────────────────────────────────────────────────────────

    fun getAllHistory(): Flow<List<DailyHistory>> = historyDao.getAllHistory()

    fun getRecentHistory(days: Int = 30): Flow<List<DailyHistory>> =
        historyDao.getRecentHistory(days)

    fun getHistoryInRange(start: String, end: String): Flow<List<DailyHistory>> =
        historyDao.getHistoryInRange(start, end)

    suspend fun getHistoryForDate(date: String): DailyHistory? =
        historyDao.getHistoryForDate(date)

    suspend fun getMaxStreak(): Int = historyDao.getMaxStreak() ?: 0

    suspend fun getCurrentStreak(): Int {
        var streak = 0
        var date = LocalDate.now()
        while (true) {
            val dateStr = date.format(dateFormatter)
            val history = historyDao.getHistoryForDate(dateStr) ?: break
            if (history.totalCompleted > 0 && history.totalCompleted >= history.totalScheduled) {
                streak++
                date = date.minusDays(1)
            } else {
                break
            }
        }
        return streak
    }

    /**
     * Recalculate and persist daily history snapshot for a given date.
     */
    suspend fun refreshDailyHistory(date: String) {
        val total = entryDao.getTotalCountForDate(date)
        val completed = entryDao.getCompletedCountForDate(date)
        val missed = entryDao.getMissedCountForDate(date)

        val currentStreak = getCurrentStreak()
        val prev = historyDao.getHistoryForDate(date)

        historyDao.insertHistory(
            DailyHistory(
                date = date,
                totalScheduled = total,
                totalCompleted = completed,
                totalMissed = missed,
                streakDay = currentStreak,
                mealCompleted = prev?.mealCompleted ?: 0,
                mealTotal = prev?.mealTotal ?: 0,
                medicineCompleted = prev?.medicineCompleted ?: 0,
                medicineTotal = prev?.medicineTotal ?: 0
            )
        )
    }

    // ── Preferences ──────────────────────────────────────────────────────────

    suspend fun getPref(key: String): String? = prefDao.getValue(key)

    suspend fun setPref(key: String, value: String) =
        prefDao.setValue(UserPreference(key, value))

    // ── Backup / Restore ────────────────────────────────────────────────────

    suspend fun getAllEntriesForBackup(): List<HealthEntry> = entryDao.getAllEntries()
    suspend fun getAllSchedulesForBackup(): List<ReminderSchedule> = scheduleDao.getAllSchedulesOnce()
    suspend fun getAllHistoryForBackup(): List<DailyHistory> = historyDao.getAllHistoryOnce()

    suspend fun restoreEntries(entries: List<HealthEntry>) = entryDao.insertEntries(entries)
    suspend fun restoreSchedules(schedules: List<ReminderSchedule>) {
        schedules.forEach { scheduleDao.insertSchedule(it) }
    }

    // ── Utilities ────────────────────────────────────────────────────────────

    fun today(): String = LocalDate.now().format(dateFormatter)
}
