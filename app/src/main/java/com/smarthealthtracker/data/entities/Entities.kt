package com.smarthealthtracker.data.entities

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.smarthealthtracker.data.db.Converters
import kotlinx.parcelize.Parcelize

// ── Enumerations ──────────────────────────────────────────────────────────────

enum class Category { FOOD, MEDICINE }
enum class MealType { BREAKFAST, LUNCH, DINNER, SNACK }
enum class EntryStatus { PENDING, COMPLETED, MISSED, SNOOZED }
enum class DayOfWeek { MON, TUE, WED, THU, FRI, SAT, SUN }

// ── HealthEntry ───────────────────────────────────────────────────────────────

/**
 * A single health entry: one meal or one medication dose for a specific date/time.
 */
@Parcelize
@Entity(tableName = "health_entries")
data class HealthEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val notes: String = "",
    val scheduledTime: String,
    val date: String,
    val category: Category,
    val mealType: MealType? = null,
    val dosage: String = "",
    val status: EntryStatus = EntryStatus.PENDING,
    val completedAt: Long? = null,
    val scheduleId: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
) : Parcelable

// ── ReminderSchedule ──────────────────────────────────────────────────────────

/**
 * A recurring schedule that auto-generates daily HealthEntry instances.
 */
@Parcelize
@Entity(tableName = "reminder_schedules")
@TypeConverters(Converters::class)
data class ReminderSchedule(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val category: Category,
    val mealType: MealType? = null,
    val dosage: String = "",
    val notes: String = "",
    val defaultTime: String,
    val activeDays: List<DayOfWeek> = DayOfWeek.values().toList(),
    val dayTimeOverrides: Map<String, String> = emptyMap(),
    val chainDelayMinutes: Int = 0,
    val chainNextScheduleId: Long? = null,
    val isEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) : Parcelable

// ── DailyHistory ──────────────────────────────────────────────────────────────

/**
 * Aggregated daily stats snapshot used for streaks and progress charts.
 */
@Entity(tableName = "daily_history")
data class DailyHistory(
    @PrimaryKey
    val date: String,
    val totalScheduled: Int = 0,
    val totalCompleted: Int = 0,
    val totalMissed: Int = 0,
    val mealCompleted: Int = 0,
    val mealTotal: Int = 0,
    val medicineCompleted: Int = 0,
    val medicineTotal: Int = 0,
    val streakDay: Int = 0,
    val updatedAt: Long = System.currentTimeMillis()
)

// ── UserPreference ────────────────────────────────────────────────────────────

/**
 * Simple key/value preference store backed by Room.
 */
@Entity(tableName = "user_preferences")
data class UserPreference(
    @PrimaryKey
    val key: String,
    val value: String
)
