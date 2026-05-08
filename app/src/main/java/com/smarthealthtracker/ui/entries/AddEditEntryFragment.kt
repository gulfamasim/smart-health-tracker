package com.smarthealthtracker.ui.entries

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.chip.Chip
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.smarthealthtracker.R
import com.smarthealthtracker.data.entities.Category
import com.smarthealthtracker.data.entities.MealType
import com.smarthealthtracker.databinding.FragmentAddEditEntryBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar

@AndroidEntryPoint
class AddEditEntryFragment : Fragment() {

    private var _binding: FragmentAddEditEntryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AddEditEntryViewModel by viewModels()
    private val args: AddEditEntryFragmentArgs by navArgs()

    private var selectedDate: String = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    private var selectedTime: String = ""
    private var selectedCategory: Category = Category.FOOD
    private var selectedMealType: MealType = MealType.BREAKFAST
    private var repeatEndDate: String? = null

    // Day-of-week chips: index 0=Mon … 6=Sun
    private val dayChips = mutableListOf<Chip>()
    private val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    private val javaDays = listOf(
        DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddEditEntryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().title = if (args.entryId == -1L) "Add Entry" else "Edit Entry"

        setupCategoryToggle()
        setupMealTypeChips()
        setupTimePicker()
        setupRepeatDayChips()
        setupRepeatEndDate()
        setupSingleDatePicker()
        setupSaveButton()
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
    }

    private fun setupMealTypeChips() {
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

    private fun setupTimePicker() {
        // Default to current time rounded up to next 30 min
        val now = LocalTime.now()
        val defaultHour = if (now.minute >= 30) (now.hour + 1) % 24 else now.hour
        val defaultMin = if (now.minute >= 30) 0 else 30
        selectedTime = String.format("%02d:%02d", defaultHour, defaultMin)
        binding.btnPickTime.text = selectedTime

        binding.btnPickTime.setOnClickListener {
            val cal = Calendar.getInstance()
            TimePickerDialog(requireContext(), { _, hour, minute ->
                selectedTime = String.format("%02d:%02d", hour, minute)
                binding.btnPickTime.text = selectedTime
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }
    }

    private fun setupRepeatDayChips() {
        dayLabels.forEachIndexed { index, label ->
            val chip = Chip(requireContext()).apply {
                text = label
                isCheckable = true
                isChecked = false
                setOnCheckedChangeListener { _, _ -> onRepeatDaysChanged() }
            }
            binding.chipGroupDays.addView(chip)
            dayChips.add(chip)
        }
    }

    private fun onRepeatDaysChanged() {
        val anySelected = dayChips.any { it.isChecked }
        // Show end date picker and hide single date when repeating
        binding.layoutRepeatEnd.visibility = if (anySelected) View.VISIBLE else View.GONE
        binding.layoutSingleDate.visibility = if (anySelected) View.GONE else View.VISIBLE
    }

    private fun setupRepeatEndDate() {
        binding.btnRepeatEndDate.setOnClickListener {
            val constraints = CalendarConstraints.Builder()
                .setValidator(DateValidatorPointForward.now())
                .build()
            val picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Repeat until (optional)")
                .setCalendarConstraints(constraints)
                .build()
            picker.addOnPositiveButtonClickListener { millis ->
                val date = Instant.ofEpochMilli(millis)
                    .atZone(ZoneId.systemDefault()).toLocalDate()
                repeatEndDate = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                binding.btnRepeatEndDate.text = "Ends: $repeatEndDate"
            }
            picker.addOnNegativeButtonClickListener {
                repeatEndDate = null
                binding.btnRepeatEndDate.text = "No end date (repeat forever)"
            }
            picker.show(parentFragmentManager, "repeat_end_picker")
        }
    }

    private fun setupSingleDatePicker() {
        binding.btnPickDate.text = selectedDate
        // Only allow today or future dates
        val constraints = CalendarConstraints.Builder()
            .setValidator(DateValidatorPointForward.now())
            .build()
        binding.btnPickDate.setOnClickListener {
            val picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Date")
                .setCalendarConstraints(constraints)
                .build()
            picker.addOnPositiveButtonClickListener { millis ->
                val date = Instant.ofEpochMilli(millis)
                    .atZone(ZoneId.systemDefault()).toLocalDate()
                selectedDate = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                binding.btnPickDate.text = selectedDate
            }
            picker.show(parentFragmentManager, "date_picker")
        }
    }

    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener {
            if (selectedTime.isBlank()) {
                Snackbar.make(binding.root, "Please pick a time", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (binding.etTitle.text.isNullOrBlank()) {
                Snackbar.make(binding.root, "Please enter a title", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Collect selected repeat days
            val selectedDays = dayChips.mapIndexedNotNull { i, chip ->
                if (chip.isChecked) javaDays[i] else null
            }

            viewModel.saveEntry(
                title         = binding.etTitle.text.toString().trim(),
                notes         = binding.etNotes.text.toString().trim(),
                time          = selectedTime,
                category      = selectedCategory,
                mealType      = if (selectedCategory == Category.FOOD) selectedMealType else null,
                dosage        = binding.etDosage.text.toString().trim(),
                date          = selectedDate,
                scheduleAlarm = binding.switchAlarm.isChecked,
                repeatDays    = selectedDays,
                repeatEndDate = repeatEndDate
            )
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.entry.collect { entry ->
                        if (entry != null) {
                            binding.etTitle.setText(entry.title)
                            binding.etNotes.setText(entry.notes)
                            selectedTime = entry.scheduledTime
                            binding.btnPickTime.text = entry.scheduledTime
                            selectedDate = entry.date
                            binding.btnPickDate.text = entry.date
                            if (entry.category == Category.MEDICINE) {
                                binding.toggleCategory.check(R.id.btn_medicine)
                                binding.etDosage.setText(entry.dosage)
                            } else {
                                binding.toggleCategory.check(R.id.btn_food)
                            }
                        }
                    }
                }
                launch {
                    viewModel.saved.collect {
                        Snackbar.make(binding.root, "✅ Saved!", Snackbar.LENGTH_SHORT).show()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
