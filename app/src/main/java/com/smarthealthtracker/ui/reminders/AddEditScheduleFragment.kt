package com.smarthealthtracker.ui.reminders

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.smarthealthtracker.R
import com.smarthealthtracker.data.entities.Category
import com.smarthealthtracker.data.entities.DayOfWeek
import com.smarthealthtracker.data.entities.MealType
import com.smarthealthtracker.data.entities.ReminderSchedule
import com.smarthealthtracker.databinding.FragmentAddEditScheduleBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Calendar

@AndroidEntryPoint
class AddEditScheduleFragment : Fragment() {

    private var _binding: FragmentAddEditScheduleBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AddEditScheduleViewModel by viewModels()
    private val args: AddEditScheduleFragmentArgs by navArgs()

    private var selectedCategory = Category.FOOD
    private var selectedMealType = MealType.BREAKFAST
    private var defaultTime = ""
    private val selectedDays = mutableSetOf<DayOfWeek>()
    private val dayOverrides = mutableMapOf<String, String>()
    private var chainNextScheduleId: Long? = null
    private var allSchedules: List<ReminderSchedule> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddEditScheduleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().title = if (args.scheduleId == -1L) "New Schedule" else "Edit Schedule"

        setupCategoryToggle()
        setupDayChips()
        setupTimePicker()
        setupPerDayOverrides()
        setupChainSection()
        setupSave()
        observeViewModel()
    }

    private fun setupCategoryToggle() {
        binding.toggleCategory.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            selectedCategory = when (checkedId) {
                R.id.btn_food -> {
                    binding.layoutMealType.visibility = View.VISIBLE
                    binding.layoutDosage.visibility = View.GONE
                    Category.FOOD
                }
                else -> {
                    binding.layoutMealType.visibility = View.GONE
                    binding.layoutDosage.visibility = View.VISIBLE
                    Category.MEDICINE
                }
            }
        }
        binding.toggleCategory.check(R.id.btn_food)

        // Meal type chips
        MealType.values().forEach { mt ->
            val chip = Chip(requireContext()).apply {
                text = mt.name.lowercase().replaceFirstChar { it.uppercase() }
                isCheckable = true
                isChecked = mt == MealType.BREAKFAST
                setOnCheckedChangeListener { _, checked -> if (checked) selectedMealType = mt }
            }
            binding.chipGroupMealType.addView(chip)
        }
    }

    private fun setupDayChips() {
        DayOfWeek.values().forEach { day ->
            val chip = Chip(requireContext()).apply {
                text = day.name.take(3)
                isCheckable = true
                isChecked = true
                setOnCheckedChangeListener { _, checked ->
                    if (checked) selectedDays.add(day) else selectedDays.remove(day)
                }
            }
            binding.chipGroupDays.addView(chip)
            selectedDays.add(day)  // All selected by default
        }
    }

    private fun setupTimePicker() {
        binding.btnDefaultTime.setOnClickListener {
            pickTime { time ->
                defaultTime = time
                binding.btnDefaultTime.text = time
            }
        }
    }

    private fun setupPerDayOverrides() {
        binding.switchPerDayTime.setOnCheckedChangeListener { _, checked ->
            binding.layoutPerDayTimes.visibility = if (checked) View.VISIBLE else View.GONE
        }

        DayOfWeek.values().forEach { day ->
            val btn = com.google.android.material.button.MaterialButton(requireContext()).apply {
                text = "${day.name.take(3)}: (use default)"
                setOnClickListener {
                    pickTime { time ->
                        dayOverrides[day.name] = time
                        this.text = "${day.name.take(3)}: $time"
                    }
                }
            }
            binding.layoutPerDayTimes.addView(btn)
        }
    }

    private fun setupChainSection() {
        binding.switchChainReminder.setOnCheckedChangeListener { _, checked ->
            binding.layoutChain.visibility = if (checked) View.VISIBLE else View.GONE
        }
    }

    private fun setupSave() {
        binding.btnSaveSchedule.setOnClickListener {
            val chainDelay = binding.etChainDelay.text.toString().toIntOrNull() ?: 0

            // Get chain next schedule from spinner
            val chainIdx = binding.spinnerChainNext.selectedItemPosition
            chainNextScheduleId = if (chainIdx > 0 && allSchedules.isNotEmpty()) {
                allSchedules.getOrNull(chainIdx - 1)?.id
            } else null

            viewModel.save(
                title = binding.etTitle.text.toString().trim(),
                defaultTime = defaultTime,
                category = selectedCategory,
                mealType = if (selectedCategory == Category.FOOD) selectedMealType else null,
                dosage = binding.etDosage.text.toString().trim(),
                notes = binding.etNotes.text.toString().trim(),
                activeDays = selectedDays.toList(),
                dayOverrides = dayOverrides.toMap(),
                chainDelayMinutes = chainDelay,
                chainNextScheduleId = chainNextScheduleId
            )
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    viewModel.allSchedules.collect { schedules ->
                        allSchedules = schedules
                        val names = mutableListOf("None") + schedules.map { it.title }
                        binding.spinnerChainNext.adapter = ArrayAdapter(
                            requireContext(),
                            android.R.layout.simple_spinner_dropdown_item,
                            names
                        )
                    }
                }

                launch {
                    viewModel.schedule.collect { s ->
                        if (s != null) populateForm(s)
                    }
                }

                launch {
                    viewModel.saved.collect {
                        Snackbar.make(binding.root, "Schedule saved!", Snackbar.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    }
                }

                launch {
                    viewModel.error.collect { msg ->
                        Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun populateForm(s: ReminderSchedule) {
        binding.etTitle.setText(s.title)
        defaultTime = s.defaultTime
        binding.btnDefaultTime.text = s.defaultTime
        binding.etNotes.setText(s.notes)
        if (s.category == Category.MEDICINE) {
            binding.toggleCategory.check(R.id.btn_medicine)
            binding.etDosage.setText(s.dosage)
        }
        if (s.chainDelayMinutes > 0) {
            binding.switchChainReminder.isChecked = true
            binding.etChainDelay.setText(s.chainDelayMinutes.toString())
        }
    }

    private fun pickTime(onSelected: (String) -> Unit) {
        val cal = Calendar.getInstance()
        TimePickerDialog(
            requireContext(),
            { _, h, m -> onSelected(String.format("%02d:%02d", h, m)) },
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            true
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
