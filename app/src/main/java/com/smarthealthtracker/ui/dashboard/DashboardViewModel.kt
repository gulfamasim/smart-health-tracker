package com.smarthealthtracker.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smarthealthtracker.data.entities.EntryStatus
import com.smarthealthtracker.data.entities.HealthEntry
import com.smarthealthtracker.data.repository.HealthRepository
import com.smarthealthtracker.di.AlarmScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class DashboardUiState(
    val entries: List<HealthEntry> = emptyList(),
    val completedCount: Int = 0,
    val totalCount: Int = 0,
    val currentStreak: Int = 0,
    val isLoading: Boolean = true
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: HealthRepository,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    // Separate StateFlow for snackbar messages
    private val _message = MutableSharedFlow<String>()
    val message: SharedFlow<String> = _message.asSharedFlow()

    init {
        loadTodayData()
    }

    private fun loadTodayData() {
        viewModelScope.launch {
            combine(
                repository.getTodayEntries(),
                flow { emit(repository.getCurrentStreak()) }
            ) { entries, streak ->
                DashboardUiState(
                    entries = entries,
                    completedCount = entries.count { it.status == EntryStatus.COMPLETED },
                    totalCount = entries.size,
                    currentStreak = streak,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun completeEntry(entryId: Long) {
        viewModelScope.launch {
            val nextSchedule = repository.completeEntry(entryId)
            alarmScheduler.cancelAlarm(entryId)

            if (nextSchedule != null && nextSchedule.isEnabled && nextSchedule.chainDelayMinutes > 0) {
                alarmScheduler.scheduleChainAlarm(
                    entryId = nextSchedule.id,
                    delayMinutes = nextSchedule.chainDelayMinutes,
                    title = nextSchedule.title,
                    category = nextSchedule.category.name
                )
                _message.emit("✓ Done! ${nextSchedule.title} reminder set in ${nextSchedule.chainDelayMinutes} min")
            } else {
                _message.emit("✓ Marked as complete!")
            }
        }
    }

    fun deleteEntry(entry: HealthEntry) {
        viewModelScope.launch {
            alarmScheduler.cancelAlarm(entry.id)
            repository.deleteEntry(entry)
            _message.emit("Entry deleted")
        }
    }

    fun scheduleAlarmForEntry(entry: HealthEntry) {
        viewModelScope.launch {
            val today = LocalDate.now()
            val timeParts = entry.scheduledTime.split(":")
            if (timeParts.size != 2) return@launch

            val alarmTime = today
                .atTime(timeParts[0].toInt(), timeParts[1].toInt())
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()

            if (alarmTime > System.currentTimeMillis()) {
                alarmScheduler.scheduleAlarm(
                    entryId = entry.id,
                    triggerTimeMs = alarmTime,
                    title = entry.title,
                    category = entry.category.name
                )
                _message.emit("⏰ Alarm set for ${entry.scheduledTime}")
            } else {
                _message.emit("⚠️ That time has already passed today")
            }
        }
    }
}
