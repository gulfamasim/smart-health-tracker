package com.smarthealthtracker.widget

import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.smarthealthtracker.R
import com.smarthealthtracker.data.db.HealthDatabase
import com.smarthealthtracker.data.entities.Category
import com.smarthealthtracker.data.entities.EntryStatus
import com.smarthealthtracker.data.entities.HealthEntry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * RemoteViewsService providing the list of today's health entries for the home-screen widget.
 * NOTE: Hilt does not support RemoteViewsService; we access Room directly via EntryPoint.
 */
class WidgetService : RemoteViewsService() {

    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        val db = HealthDatabase.getInstance(applicationContext)
        return HealthWidgetFactory(applicationContext, db)
    }
}

class HealthWidgetFactory(
    private val context: Context,
    private val db: HealthDatabase
) : RemoteViewsService.RemoteViewsFactory {

    private var entries: List<HealthEntry> = emptyList()
    private val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    override fun onCreate() {}

    override fun onDataSetChanged() {
        val today = LocalDate.now().format(fmt)
        entries = runBlocking {
            db.healthEntryDao().getEntriesForDate(today).first()
        }
    }

    override fun onDestroy() {}
    override fun getCount() = entries.size

    override fun getViewAt(position: Int): RemoteViews {
        val entry = entries[position]
        return RemoteViews(context.packageName, R.layout.widget_entry_item).apply {
            setTextViewText(R.id.widget_entry_title, entry.title)
            setTextViewText(R.id.widget_entry_time, entry.scheduledTime)
            setImageViewResource(
                R.id.widget_entry_icon,
                if (entry.category == Category.MEDICINE) R.drawable.ic_medicine
                else R.drawable.ic_food
            )
            setImageViewResource(
                R.id.widget_status_icon,
                if (entry.status == EntryStatus.COMPLETED) R.drawable.ic_check_circle
                else R.drawable.ic_pending
            )
        }
    }

    override fun getLoadingView(): RemoteViews? = null
    override fun getViewTypeCount() = 1
    override fun getItemId(position: Int) = entries[position].id
    override fun hasStableIds() = true
}
