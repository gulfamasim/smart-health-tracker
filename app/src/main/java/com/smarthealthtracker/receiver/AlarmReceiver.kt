package com.smarthealthtracker.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.smarthealthtracker.service.AlarmService
import dagger.hilt.android.AndroidEntryPoint

/**
 * BroadcastReceiver that fires when a scheduled alarm triggers.
 * Delegates to AlarmService for notification display and chain logic.
 */
@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val entryId = intent.getLongExtra(EXTRA_ENTRY_ID, -1L)
        if (entryId == -1L) return

        val serviceIntent = Intent(context, AlarmService::class.java).apply {
            putExtra(EXTRA_ENTRY_ID, entryId)
            putExtra(EXTRA_TITLE, intent.getStringExtra(EXTRA_TITLE) ?: "Health Reminder")
            putExtra(EXTRA_CATEGORY, intent.getStringExtra(EXTRA_CATEGORY) ?: "FOOD")
        }

        context.startForegroundService(serviceIntent)
    }

    companion object {
        const val EXTRA_ENTRY_ID = "extra_entry_id"
        const val EXTRA_TITLE    = "extra_title"
        const val EXTRA_CATEGORY = "extra_category"
    }
}
