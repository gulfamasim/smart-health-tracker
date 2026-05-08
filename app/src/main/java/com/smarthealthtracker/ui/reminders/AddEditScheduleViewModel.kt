package com.smarthealthtracker.ui.reminders

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smarthealthtracker.data.entities.*
import com.smarthealthtracker.data.repository.HealthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditScheduleViewModel @Inject constructor(
    private val repository: HealthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val scheduleId: Long = savedStateHandle.get<Long>("scheduleId") ?: -1L
    val isEditMode get() = scheduleId != -1L

    private val _schedule = MutableStateFlow<ReminderSchedule?>(null)
    val schedule = _schedule.asStateFlow()

    val allSchedules = repository.getAllSchedules()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _saved = MutableSharedFlow<Boolean>()
    val saved = _saved.asSharedFlow()

    private val _error = MutableSharedFlow<String>()
    val error = _error.asSharedFlow()

    init {
        if (isEditMode) {
            viewModelScope.launch {
                _schedule.value = repository.getScheduleById(scheduleId)
            }
        }
    }

    fun save(
        title: String, defaultTime: String, category: Category,
        mealType: MealType?, dosage: String, notes: String,
        activeDays: List<DayOfWeek>, dayOverrides: Map<String, String>,
        chainDelayMinutes: Int, chainNextScheduleId: Long?
    ) {
        if (title.isBlank()) {
            viewModelScope.launch { _error.emit("Title is required") }
            return
        }
        if (defaultTime.isBlank()) {
            viewModelScope.launch { _error.emit("Time is required") }
            return
        }
        if (activeDays.isEmpty()) {
            viewModelScope.launch { _error.emit("Select at least one day") }
            return
        }

        viewModelScope.launch {
            val s = ReminderSchedule(
                id = if (isEditMode) scheduleId else 0,
                title = title, defaultTime = defaultTime, category = category,
                mealType = mealType, dosage = dosage, notes = notes,
                activeDays = activeDays, dayTimeOverrides = dayOverrides,
                chainDelayMinutes = chainDelayMinutes,
                chainNextScheduleId = chainNextScheduleId
            )
            if (isEditMode) repository.updateSchedule(s) else repository.insertSchedule(s)
            _saved.emit(true)
        }
    }
}
