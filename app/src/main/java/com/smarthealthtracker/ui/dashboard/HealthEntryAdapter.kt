package com.smarthealthtracker.ui.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.smarthealthtracker.R
import com.smarthealthtracker.data.entities.Category
import com.smarthealthtracker.data.entities.EntryStatus
import com.smarthealthtracker.data.entities.HealthEntry
import com.smarthealthtracker.databinding.ItemHealthEntryBinding

/**
 * RecyclerView adapter for daily entry timeline.
 * Uses DiffUtil for efficient updates.
 */
class HealthEntryAdapter(
    private val onComplete: (HealthEntry) -> Unit,
    private val onEdit: (HealthEntry) -> Unit,
    private val onDelete: (HealthEntry) -> Unit,
    private val onAlarm: (HealthEntry) -> Unit
) : ListAdapter<HealthEntry, HealthEntryAdapter.EntryViewHolder>(EntryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EntryViewHolder {
        val binding = ItemHealthEntryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return EntryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EntryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class EntryViewHolder(
        private val binding: ItemHealthEntryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(entry: HealthEntry) {
            val ctx = binding.root.context

            binding.tvTitle.text = entry.title
            binding.tvTime.text = entry.scheduledTime
            binding.tvNotes.text = entry.notes.ifEmpty { "" }
            binding.tvNotes.visibility = if (entry.notes.isBlank()) View.GONE else View.VISIBLE

            // Category chip color + icon
            if (entry.category == Category.MEDICINE) {
                binding.ivCategoryIcon.setImageResource(R.drawable.ic_medicine)
                binding.chipCategory.text = entry.dosage.ifBlank { "Medicine" }
                binding.cardEntry.strokeColor =
                    ContextCompat.getColor(ctx, R.color.medicine_color)
            } else {
                binding.ivCategoryIcon.setImageResource(R.drawable.ic_food)
                binding.chipCategory.text = entry.mealType?.name?.lowercase()
                    ?.replaceFirstChar { it.uppercase() } ?: "Meal"
                binding.cardEntry.strokeColor =
                    ContextCompat.getColor(ctx, R.color.food_color)
            }

            // Status styling
            when (entry.status) {
                EntryStatus.COMPLETED -> {
                    binding.cardEntry.alpha = 0.65f
                    binding.tvTitle.paintFlags =
                        binding.tvTitle.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
                    binding.btnComplete.isEnabled = false
                    binding.btnComplete.text = if (entry.category == Category.MEDICINE)
                        "Taken ✓" else "Eaten ✓"
                    binding.ivStatusBadge.setImageResource(R.drawable.ic_check_circle)
                    binding.ivStatusBadge.visibility = View.VISIBLE
                }
                EntryStatus.MISSED -> {
                    binding.cardEntry.alpha = 0.5f
                    binding.btnComplete.isEnabled = false
                    binding.btnComplete.text = "Missed"
                    binding.ivStatusBadge.setImageResource(R.drawable.ic_missed)
                    binding.ivStatusBadge.visibility = View.VISIBLE
                }
                EntryStatus.SNOOZED -> {
                    binding.cardEntry.alpha = 0.85f
                    binding.btnComplete.isEnabled = true
                    binding.btnComplete.text = "Snoozed"
                    binding.ivStatusBadge.setImageResource(R.drawable.ic_snooze)
                    binding.ivStatusBadge.visibility = View.VISIBLE
                }
                else -> {
                    binding.cardEntry.alpha = 1f
                    binding.tvTitle.paintFlags =
                        binding.tvTitle.paintFlags and android.graphics.Paint.STRIKE_THRU_TEXT_FLAG.inv()
                    binding.btnComplete.isEnabled = true
                    binding.btnComplete.text = if (entry.category == Category.MEDICINE)
                        "Mark Taken" else "Mark Eaten"
                    binding.ivStatusBadge.visibility = View.GONE
                }
            }

            // Click listeners
            binding.btnComplete.setOnClickListener { onComplete(entry) }
            binding.btnEdit.setOnClickListener { onEdit(entry) }
            binding.btnDelete.setOnClickListener { onDelete(entry) }
            binding.btnAlarm.setOnClickListener { onAlarm(entry) }
            binding.root.setOnClickListener { onEdit(entry) }
        }
    }

    class EntryDiffCallback : DiffUtil.ItemCallback<HealthEntry>() {
        override fun areItemsTheSame(old: HealthEntry, new: HealthEntry) = old.id == new.id
        override fun areContentsTheSame(old: HealthEntry, new: HealthEntry) = old == new
    }
}
