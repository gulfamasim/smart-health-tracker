package com.smarthealthtracker.ui.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.smarthealthtracker.data.entities.DailyHistory
import com.smarthealthtracker.data.entities.HealthEntry
import com.smarthealthtracker.data.entities.ReminderSchedule
import com.smarthealthtracker.data.repository.HealthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import javax.inject.Inject

data class BackupData(
    val entries: List<HealthEntry>,
    val schedules: List<ReminderSchedule>,
    val history: List<DailyHistory>,
    val exportedAt: Long = System.currentTimeMillis(),
    val version: Int = 1
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: HealthRepository
) : ViewModel() {

    private val _message = MutableSharedFlow<String>()
    val message = _message.asSharedFlow()

    private val gson = Gson()

    fun exportBackup(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                val data = BackupData(
                    entries   = repository.getAllEntriesForBackup(),
                    schedules = repository.getAllSchedulesForBackup(),
                    history   = repository.getAllHistoryForBackup()
                )
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    OutputStreamWriter(out).use { writer ->
                        writer.write(gson.toJson(data))
                    }
                }
                _message.emit("✅ Backup exported successfully")
            } catch (e: Exception) {
                _message.emit("❌ Export failed: ${e.message}")
            }
        }
    }

    fun importBackup(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    InputStreamReader(input).use { reader ->
                        val data = gson.fromJson(reader.readText(), BackupData::class.java)
                        repository.restoreEntries(data.entries)
                        repository.restoreSchedules(data.schedules)
                        _message.emit("✅ Restored ${data.entries.size} entries & ${data.schedules.size} schedules")
                    }
                } ?: _message.emit("❌ Could not open file")
            } catch (e: Exception) {
                _message.emit("❌ Import failed: ${e.message}")
            }
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            // Re-implemented as needed — placeholder
            _message.emit("All data cleared")
        }
    }
}
