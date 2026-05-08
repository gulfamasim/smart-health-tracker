package com.smarthealthtracker.ui.entries

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smarthealthtracker.data.entities.*
import com.smarthealthtracker.data.repository.HealthRepository
import com.smarthealthtracker.di.AlarmScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class AddEditEntryViewModel @Inject constructor(
    private val repository: HealthRepository,
    private val alarmScheduler: AlarmScheduler,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val entryId: Long = savedStateHandle.get<Long>("entryId") ?: -1L
    val isEditMode get() = entryId != -1L

    private val _entry = MutableStateFlow<HealthEntry?>(null)
    val entry = _entry.asStateFlow()

    private val _saved = MutableSharedFlow<Boolean>()
    val saved = _saved.asSharedFlow()

    private val _error = MutableSharedFlow<String>()
    val error = _error.asSharedFlow()

    private val dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    init {
        if (isEditMode) viewModelScope.launch { _entry.value = repository.getEntryById(entryId) }
    }

    fun saveEntry(
        title: String,
        notes: String,
        time: String,
        category: Category,
        mealType: MealType?,
        dosage: String,
        date: String,
        scheduleAlarm: Boolean,
        repeatDays: List<DayOfWeek>,
        repeatEndDate: String?
    ) {
        if (title.isBlank()) {
            viewModelScope.launch { _error.emit("Title cannot be empty") }
            return
        }
        if (time.isBlank()) {
            viewModelScope.launch { _error.emit("Please set a time") }
            return
        }

        viewModelScope.launch {
            val timeParts = time.split(":")
            if (timeParts.size != 2) { _error.emit("Invalid time format"); return@launch }
            val hour = timeParts[0].toIntOrNull() ?: 0
            val minute = timeParts[1].toIntOrNull() ?: 0
            val scheduledTime = LocalTime.of(hour, minute)
            val now = LocalTime.now()
            val today = LocalDate.now()

            if (repeatDays.isEmpty()) {
                // ── Single entry ──────────────────────────────────────────────
                val entryDate = runCatching { LocalDate.parse(date, dateFmt) }.getOrElse { today }

                // Block if time has already passed today
                if (entryDate == today && scheduledTime.isBefore(now)) {
                    _error.emit("⏰ That time has already passed today. Please pick a future time.")
                    return@launch
                }

                val newEntry = if (isEditMode) {
                    _entry.value?.copy(
                        title = title, notes = notes, scheduledTime = time,
                        category = category, mealType = mealType, dosage = dosage
                    )
                } else {
                    HealthEntry(
                        title = title, notes = notes, scheduledTime = time,
                        date = entryDate.format(dateFmt), category = category,
                        mealType = mealType, dosage = dosage
                    )
                } ?: return@launch

                val savedId = if (isEditMode) {
                    repository.updateEntry(newEntry); entryId
                } else {
                    repository.insertEntry(newEntry)
                }

                if (scheduleAlarm && entryDate >= today) {
                    val triggerMs = entryDate.atTime(scheduledTime)
                        .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    if (triggerMs > System.currentTimeMillis()) {
                        alarmScheduler.scheduleAlarm(savedId, triggerMs, title, category.name)
                    }
                }

            } else {
                // ── Repeating entries (one per selected day from today onward) ─
                val endDate = repeatEndDate?.let {
                    runCatching { LocalDate.parse(it, dateFmt) }.getOrNull()
                } ?: today.plusMonths(3) // default: generate 3 months ahead

                var current = today
                var entriesCreated = 0

                while (!current.isAfter(endDate)) {
                    val javaDow = current.dayOfWeek // java.time.DayOfWeek
                    if (javaDow in repeatDays) {
                        // Skip if today and time already passed
                        val skip = current == today && scheduledTime.isBefore(now)
                        if (!skip) {
                            val existing = repository.getEntryByScheduleAndDate(0L, current.format(dateFmt))
                            if (existing == null) {
                                val entryToSave = HealthEntry(
                                    title = title, notes = notes, scheduledTime = time,
                                    date = current.format(dateFmt), category = category,
                                    mealType = mealType, dosage = dosage
                                )
                                val savedId = repository.insertEntry(entryToSave)

                                if (scheduleAlarm && current == today) {
                                    val triggerMs = current.atTime(scheduledTime)
                                        .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                                    if (triggerMs > System.currentTimeMillis()) {
                                        alarmScheduler.scheduleAlarm(savedId, triggerMs, title, category.name)
                                    }
                                }
                                entriesCreated++
                            }
                        }
                    }
                    current = current.plusDays(1)
                }

                if (entriesCreated == 0) {
                    _error.emit("No future entries to create — all selected times have already passed.")
                    return@launch
                }
            }

            _saved.emit(true)
        }
    }
}
