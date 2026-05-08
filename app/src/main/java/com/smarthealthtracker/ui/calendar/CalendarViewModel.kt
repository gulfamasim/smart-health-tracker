package com.smarthealthtracker.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smarthealthtracker.data.entities.DailyHistory
import com.smarthealthtracker.data.entities.HealthEntry
import com.smarthealthtracker.data.repository.HealthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val repository: HealthRepository
) : ViewModel() {

    private val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate = _selectedDate.asStateFlow()

    val selectedDateEntries: StateFlow<List<HealthEntry>> = _selectedDate
        .flatMapLatest { date ->
            repository.getEntriesForDate(date.format(fmt))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // History for last 3 months to color calendar cells
    val monthHistory: StateFlow<List<DailyHistory>> = repository
        .getRecentHistory(90)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }
}
