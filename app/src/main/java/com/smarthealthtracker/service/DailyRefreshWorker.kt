package com.smarthealthtracker.service

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.smarthealthtracker.data.entities.EntryStatus
import com.smarthealthtracker.data.repository.HealthRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

/**
 * Runs once per day at midnight+1 min.
 * - Marks all still-pending yesterday entries as MISSED
 * - Generates today's entries from schedules
 * - Updates daily history snapshot
 */
@HiltWorker
class DailyRefreshWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: HealthRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val yesterday = LocalDate.now().minusDays(1)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

            // Mark yesterday's uncompleted entries as missed
            val yesterdayEntries = repository.getEntriesForDate(yesterday).first()
            yesterdayEntries
                .filter { it.status == EntryStatus.PENDING || it.status == EntryStatus.SNOOZED }
                .forEach { entry ->
                    repository.updateEntry(entry.copy(status = EntryStatus.MISSED))
                }

            // Refresh history for yesterday
            repository.refreshDailyHistory(yesterday)

            // Generate today's entries
            repository.generateTodayEntries()

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "daily_refresh_work"

        fun schedule(context: Context) {
            val now = LocalTime.now()
            val midnight = LocalTime.MIDNIGHT
            // Calculate delay until next midnight + 1 minute
            val secondsUntilMidnight = if (now.isBefore(midnight)) {
                now.until(midnight, java.time.temporal.ChronoUnit.SECONDS)
            } else {
                (24 * 3600) - now.toSecondOfDay().toLong()
            } + 60L // +1 minute buffer

            val request = PeriodicWorkRequestBuilder<DailyRefreshWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(secondsUntilMidnight, TimeUnit.SECONDS)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiresBatteryNotLow(false)
                        .build()
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }
    }
}
