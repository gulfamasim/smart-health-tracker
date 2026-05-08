package com.smarthealthtracker.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.smarthealthtracker.data.dao.DailyHistoryDao
import com.smarthealthtracker.data.dao.HealthEntryDao
import com.smarthealthtracker.data.dao.ReminderScheduleDao
import com.smarthealthtracker.data.dao.UserPreferenceDao
import com.smarthealthtracker.data.entities.DailyHistory
import com.smarthealthtracker.data.entities.HealthEntry
import com.smarthealthtracker.data.entities.ReminderSchedule
import com.smarthealthtracker.data.entities.UserPreference

@Database(
    entities = [
        HealthEntry::class,
        ReminderSchedule::class,
        DailyHistory::class,
        UserPreference::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class HealthDatabase : RoomDatabase() {

    abstract fun healthEntryDao(): HealthEntryDao
    abstract fun reminderScheduleDao(): ReminderScheduleDao
    abstract fun dailyHistoryDao(): DailyHistoryDao
    abstract fun userPreferenceDao(): UserPreferenceDao

    companion object {
        @Volatile private var INSTANCE: HealthDatabase? = null

        /**
         * Fallback singleton for non-Hilt contexts (e.g. Widget RemoteViewsService).
         * In normal app code, prefer the Hilt-injected instance from DatabaseModule.
         */
        fun getInstance(context: Context): HealthDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    HealthDatabase::class.java,
                    "smart_health_db"
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
    }
}
