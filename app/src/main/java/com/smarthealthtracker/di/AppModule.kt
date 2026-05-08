package com.smarthealthtracker.di

import android.content.Context
import androidx.room.Room
import com.smarthealthtracker.data.dao.DailyHistoryDao
import com.smarthealthtracker.data.dao.HealthEntryDao
import com.smarthealthtracker.data.dao.ReminderScheduleDao
import com.smarthealthtracker.data.dao.UserPreferenceDao
import com.smarthealthtracker.data.db.HealthDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module providing the Room database and all DAOs.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): HealthDatabase =
        Room.databaseBuilder(context, HealthDatabase::class.java, "smart_health_db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideHealthEntryDao(db: HealthDatabase): HealthEntryDao = db.healthEntryDao()
    @Provides fun provideScheduleDao(db: HealthDatabase): ReminderScheduleDao = db.reminderScheduleDao()
    @Provides fun provideHistoryDao(db: HealthDatabase): DailyHistoryDao = db.dailyHistoryDao()
    @Provides fun providePreferenceDao(db: HealthDatabase): UserPreferenceDao = db.userPreferenceDao()
}

/**
 * Hilt module binding the AlarmScheduler interface to its implementation.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAlarmScheduler(@ApplicationContext context: Context): AlarmScheduler =
        AlarmSchedulerImpl(context)
}
