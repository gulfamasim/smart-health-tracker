package com.smarthealthtracker.ui.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smarthealthtracker.data.entities.DailyHistory
import com.smarthealthtracker.data.repository.HealthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StatsUiState(
    val recentHistory: List<DailyHistory> = emptyList(),
    val currentStreak: Int = 0,
    val maxStreak: Int = 0,
    val weeklyCompletion: Float = 0f,  // 0..1
    val totalCompleted: Int = 0,
    val totalMissed: Int = 0,
    val isLoading: Boolean = true
)

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val repository: HealthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.getRecentHistory(30),
                flow { emit(repository.getCurrentStreak()) },
                flow { emit(repository.getMaxStreak()) }
            ) { history, streak, maxStreak ->
                val last7 = history.takeLast(7)
                val weekTotal = last7.sumOf { it.totalScheduled }
                val weekDone  = last7.sumOf { it.totalCompleted }
                val weekComp  = if (weekTotal > 0) weekDone.toFloat() / weekTotal else 0f

                StatsUiState(
                    recentHistory = history,
                    currentStreak = streak,
                    maxStreak = maxStreak,
                    weeklyCompletion = weekComp,
                    totalCompleted = history.sumOf { it.totalCompleted },
                    totalMissed = history.sumOf { it.totalMissed },
                    isLoading = false
                )
            }.collect { _uiState.value = it }
        }
    }
}
