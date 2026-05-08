package com.smarthealthtracker.data.dao

import androidx.room.*
import com.smarthealthtracker.data.entities.*
import kotlinx.coroutines.flow.Flow

// ───────────────────────────────────────────────────────────────────────────────
//  HEALTH ENTRY DAO
// ───────────────────────────────────────────────────────────────────────────────

@Dao
interface HealthEntryDao {

    @Query("SELECT * FROM health_entries WHERE date = :date ORDER BY scheduledTime ASC")
    fun getEntriesForDate(date: String): Flow<List<HealthEntry>>

    @Query("SELECT * FROM health_entries WHERE date = :date AND category = :category ORDER BY scheduledTime ASC")
    fun getEntriesForDateByCategory(date: String, category: Category): Flow<List<HealthEntry>>

    @Query("SELECT * FROM health_entries WHERE id = :id")
    suspend fun getEntryById(id: Long): HealthEntry?

    @Query("SELECT * FROM health_entries WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC, scheduledTime ASC")
    fun getEntriesInRange(startDate: String, endDate: String): Flow<List<HealthEntry>>

    @Query("""
        SELECT * FROM health_entries 
        WHERE status = 'PENDING' AND date = :today 
        ORDER BY scheduledTime ASC 
        LIMIT 1
    """)
    suspend fun getNextPendingEntry(today: String): HealthEntry?

    @Query("SELECT COUNT(*) FROM health_entries WHERE date = :date AND status = 'COMPLETED'")
    suspend fun getCompletedCountForDate(date: String): Int

    @Query("SELECT COUNT(*) FROM health_entries WHERE date = :date")
    suspend fun getTotalCountForDate(date: String): Int

    @Query("SELECT COUNT(*) FROM health_entries WHERE date = :date AND status = 'MISSED'")
    suspend fun getMissedCountForDate(date: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: HealthEntry): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntries(entries: List<HealthEntry>)

    @Update
    suspend fun updateEntry(entry: HealthEntry)

    @Delete
    suspend fun deleteEntry(entry: HealthEntry)

    @Query("DELETE FROM health_entries WHERE id = :id")
    suspend fun deleteEntryById(id: Long)

    @Query("UPDATE health_entries SET status = :status, completedAt = :completedAt WHERE id = :id")
    suspend fun updateEntryStatus(id: Long, status: EntryStatus, completedAt: Long?)

    @Query("SELECT * FROM health_entries WHERE scheduleId = :scheduleId AND date = :date LIMIT 1")
    suspend fun getEntryByScheduleAndDate(scheduleId: Long, date: String): HealthEntry?

    // For backup/restore
    @Query("SELECT * FROM health_entries ORDER BY date DESC")
    suspend fun getAllEntries(): List<HealthEntry>

    @Query("DELETE FROM health_entries WHERE date < :cutoffDate")
    suspend fun deleteOldEntries(cutoffDate: String)
}

// ───────────────────────────────────────────────────────────────────────────────
//  REMINDER SCHEDULE DAO
// ───────────────────────────────────────────────────────────────────────────────

@Dao
interface ReminderScheduleDao {

    @Query("SELECT * FROM reminder_schedules WHERE isEnabled = 1 ORDER BY defaultTime ASC")
    fun getAllActiveSchedules(): Flow<List<ReminderSchedule>>

    @Query("SELECT * FROM reminder_schedules ORDER BY defaultTime ASC")
    fun getAllSchedules(): Flow<List<ReminderSchedule>>

    @Query("SELECT * FROM reminder_schedules WHERE id = :id")
    suspend fun getScheduleById(id: Long): ReminderSchedule?

    @Query("SELECT * FROM reminder_schedules WHERE category = :category AND isEnabled = 1")
    fun getSchedulesByCategory(category: Category): Flow<List<ReminderSchedule>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: ReminderSchedule): Long

    @Update
    suspend fun updateSchedule(schedule: ReminderSchedule)

    @Delete
    suspend fun deleteSchedule(schedule: ReminderSchedule)

    @Query("UPDATE reminder_schedules SET isEnabled = :enabled WHERE id = :id")
    suspend fun setEnabled(id: Long, enabled: Boolean)

    @Query("SELECT * FROM reminder_schedules ORDER BY defaultTime ASC")
    suspend fun getAllSchedulesOnce(): List<ReminderSchedule>
}

// ───────────────────────────────────────────────────────────────────────────────
//  DAILY HISTORY DAO
// ───────────────────────────────────────────────────────────────────────────────

@Dao
interface DailyHistoryDao {

    @Query("SELECT * FROM daily_history ORDER BY date DESC")
    fun getAllHistory(): Flow<List<DailyHistory>>

    @Query("SELECT * FROM daily_history ORDER BY date DESC LIMIT :limit")
    fun getRecentHistory(limit: Int): Flow<List<DailyHistory>>

    @Query("SELECT * FROM daily_history WHERE date = :date")
    suspend fun getHistoryForDate(date: String): DailyHistory?

    @Query("SELECT * FROM daily_history WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getHistoryInRange(startDate: String, endDate: String): Flow<List<DailyHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: DailyHistory)

    @Query("SELECT MAX(streakDay) FROM daily_history")
    suspend fun getMaxStreak(): Int?

    @Query("SELECT * FROM daily_history ORDER BY date DESC LIMIT 1")
    suspend fun getLatestHistory(): DailyHistory?

    // For backup
    @Query("SELECT * FROM daily_history ORDER BY date DESC")
    suspend fun getAllHistoryOnce(): List<DailyHistory>
}

// ───────────────────────────────────────────────────────────────────────────────
//  USER PREFERENCE DAO
// ───────────────────────────────────────────────────────────────────────────────

@Dao
interface UserPreferenceDao {

    @Query("SELECT value FROM user_preferences WHERE `key` = :key")
    suspend fun getValue(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setValue(pref: UserPreference)

    @Query("DELETE FROM user_preferences WHERE `key` = :key")
    suspend fun deleteKey(key: String)

    @Query("SELECT * FROM user_preferences")
    suspend fun getAll(): List<UserPreference>
}
