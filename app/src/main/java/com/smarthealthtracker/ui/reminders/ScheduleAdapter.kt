package com.smarthealthtracker.ui.reminders

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.smarthealthtracker.R
import com.smarthealthtracker.data.entities.Category
import com.smarthealthtracker.data.entities.ReminderSchedule
import com.smarthealthtracker.databinding.ItemScheduleBinding

class ScheduleAdapter(
    private val onToggle: (Long, Boolean) -> Unit,
    private val onEdit: (ReminderSchedule) -> Unit,
    private val onDelete: (ReminderSchedule) -> Unit
) : ListAdapter<ReminderSchedule, ScheduleAdapter.ViewHolder>(Diff()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemScheduleBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    inner class ViewHolder(private val b: ItemScheduleBinding) :
        RecyclerView.ViewHolder(b.root) {

        fun bind(s: ReminderSchedule) {
            b.tvScheduleTitle.text = s.title
            b.tvScheduleTime.text = s.defaultTime
            b.tvCategory.text = s.category.name.lowercase().replaceFirstChar { it.uppercase() }
            b.ivCategoryIcon.setImageResource(
                if (s.category == Category.MEDICINE) R.drawable.ic_medicine
                else R.drawable.ic_food
            )

            // Active days summary
            b.tvActiveDays.text = s.activeDays.joinToString(", ") {
                it.name.take(3).lowercase().replaceFirstChar { c -> c.uppercase() }
            }

            // Chain info
            if (s.chainDelayMinutes > 0 && s.chainNextScheduleId != null) {
                b.tvChainInfo.text = "→ next in ${s.chainDelayMinutes} min"
                b.tvChainInfo.visibility = android.view.View.VISIBLE
            } else {
                b.tvChainInfo.visibility = android.view.View.GONE
            }

            // Toggle switch (programmatic change shouldn't fire listener)
            b.switchEnabled.setOnCheckedChangeListener(null)
            b.switchEnabled.isChecked = s.isEnabled
            b.switchEnabled.setOnCheckedChangeListener { _, checked ->
                onToggle(s.id, checked)
            }

            b.btnEditSchedule.setOnClickListener { onEdit(s) }
            b.btnDeleteSchedule.setOnClickListener { onDelete(s) }
            b.root.setOnClickListener { onEdit(s) }
        }
    }

    class Diff : DiffUtil.ItemCallback<ReminderSchedule>() {
        override fun areItemsTheSame(a: ReminderSchedule, b: ReminderSchedule) = a.id == b.id
        override fun areContentsTheSame(a: ReminderSchedule, b: ReminderSchedule) = a == b
    }
}
