package com.smarthealthtracker.ui.reminders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smarthealthtracker.data.entities.ReminderSchedule
import com.smarthealthtracker.data.repository.HealthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RemindersViewModel @Inject constructor(
    private val repository: HealthRepository
) : ViewModel() {

    val schedules = repository.getAllSchedules()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleSchedule(id: Long, enabled: Boolean) {
        viewModelScope.launch { repository.setScheduleEnabled(id, enabled) }
    }

    fun deleteSchedule(schedule: ReminderSchedule) {
        viewModelScope.launch { repository.deleteSchedule(schedule) }
    }
}
