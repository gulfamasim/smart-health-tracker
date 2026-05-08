package com.smarthealthtracker.ui.calendar

import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.gridlayout.widget.GridLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.smarthealthtracker.R
import com.smarthealthtracker.databinding.FragmentCalendarBinding
import com.smarthealthtracker.ui.dashboard.HealthEntryAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@AndroidEntryPoint
class CalendarFragment : Fragment() {

    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CalendarViewModel by viewModels()

    private val completionMap = mutableMapOf<LocalDate, Float>()
    private var selectedDate: LocalDate = LocalDate.now()
    private var displayMonth: YearMonth = YearMonth.now()
    private lateinit var entryAdapter: HealthEntryAdapter
    private val dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    /**
     * Resolve a theme color attribute at runtime.
     * This correctly returns the light OR dark value depending on the active theme.
     */
    private fun themeColor(attrResId: Int): Int {
        val typedValue = TypedValue()
        return if (requireContext().theme.resolveAttribute(attrResId, typedValue, true))
            typedValue.data
        else 0xFF888888.toInt()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupEntriesList()
        buildDowHeader()
        setupNavButtons()
        renderMonth()
        observeData()
    }

    private fun setupEntriesList() {
        entryAdapter = HealthEntryAdapter(
            onComplete = {}, onEdit = {}, onDelete = {}, onAlarm = {}
        )
        binding.recyclerDayEntries.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = entryAdapter
        }
    }

    private fun buildDowHeader() {
        binding.layoutDowHeader.removeAllViews()
        // Use material attr for onSurface — adapts to dark/light automatically
        val matOnSurface = com.google.android.material.R.attr.colorOnSurface
        val textColor = themeColor(matOnSurface)
        listOf("Sun","Mon","Tue","Wed","Thu","Fri","Sat").forEach { label ->
            val tv = TextView(requireContext()).apply {
                text = label
                gravity = Gravity.CENTER
                textSize = 12f
                setTextColor(textColor)
                alpha = 0.6f
                layoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.MATCH_PARENT, 1f
                )
            }
            binding.layoutDowHeader.addView(tv)
        }
    }

    private fun setupNavButtons() {
        binding.btnPrevMonth.setOnClickListener {
            displayMonth = displayMonth.minusMonths(1)
            renderMonth()
        }
        binding.btnNextMonth.setOnClickListener {
            displayMonth = displayMonth.plusMonths(1)
            renderMonth()
        }
    }

    private fun renderMonth() {
        binding.tvMonthYear.text =
            displayMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault()) +
                    " ${displayMonth.year}"

        binding.gridCalendar.removeAllViews()

        val firstDay    = displayMonth.atDay(1)
        val startOffset = firstDay.dayOfWeek.value % 7   // Sun=0 … Sat=6
        val daysInMonth = displayMonth.lengthOfMonth()
        val today       = LocalDate.now()
        val density     = resources.displayMetrics.density
        val cellH       = (48 * density).toInt()
        val cellW       = resources.displayMetrics.widthPixels / 7

        // Resolve theme colors once — correct for current dark/light mode
        val colorOnSurface = themeColor(com.google.android.material.R.attr.colorOnSurface)
        val colorPrimary   = themeColor(com.google.android.material.R.attr.colorPrimary)
        val colorOnPrimary = themeColor(com.google.android.material.R.attr.colorOnPrimary)

        for (i in 0 until 42) {
            val dayNum    = i - startOffset + 1
            val inMonth   = dayNum in 1..daysInMonth
            val date      = if (inMonth) displayMonth.atDay(dayNum) else null

            val frame = FrameLayout(requireContext()).apply {
                layoutParams = ViewGroup.LayoutParams(cellW, cellH)
            }

            val tv = TextView(requireContext()).apply {
                text = date?.dayOfMonth?.toString() ?: ""
                gravity = Gravity.CENTER
                textSize = 15f
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
            }

            when {
                !inMonth || date == null -> {
                    tv.setTextColor(colorOnSurface)
                    tv.alpha = 0.2f
                }
                date == selectedDate -> {
                    // Selected: green circle, white text
                    frame.background = ContextCompat.getDrawable(
                        requireContext(), R.drawable.bg_selected_day)
                    tv.setTextColor(colorOnPrimary)
                    tv.setTypeface(null, Typeface.BOLD)
                }
                date == today -> {
                    // Today: primary-colored bold text, no circle
                    tv.setTextColor(colorPrimary)
                    tv.setTypeface(null, Typeface.BOLD)
                }
                date.isAfter(today) -> {
                    // Future: slightly dimmed
                    tv.setTextColor(colorOnSurface)
                    tv.alpha = 0.55f
                }
                else -> {
                    // Past: full opacity
                    tv.setTextColor(colorOnSurface)
                }
            }

            frame.addView(tv)

            // Completion dot — only on past/today with data
            if (inMonth && date != null && !date.isAfter(today)) {
                completionMap[date]?.let { ratio ->
                    val dotPx = (7 * density).toInt()
                    val dotColor = when {
                        ratio >= 1f -> ContextCompat.getColor(requireContext(), R.color.streak_green)
                        ratio > 0f  -> ContextCompat.getColor(requireContext(), R.color.partial_orange)
                        else        -> ContextCompat.getColor(requireContext(), R.color.missed_red)
                    }
                    val dot = View(requireContext()).apply {
                        layoutParams = FrameLayout.LayoutParams(
                            dotPx, dotPx, Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                        ).apply { bottomMargin = (3 * density).toInt() }
                        background = android.graphics.drawable.ShapeDrawable(
                            android.graphics.drawable.shapes.OvalShape()
                        ).apply { paint.color = dotColor }
                    }
                    frame.addView(dot)
                }
            }

            if (inMonth && date != null) {
                frame.isClickable = true
                frame.isFocusable = true
                frame.setOnClickListener {
                    selectedDate = date
                    viewModel.selectDate(date)
                    renderMonth()
                }
            }

            binding.gridCalendar.addView(frame, GridLayout.LayoutParams().apply {
                width      = cellW
                height     = cellH
                rowSpec    = GridLayout.spec(i / 7)
                columnSpec = GridLayout.spec(i % 7)
            })
        }
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    viewModel.monthHistory.collect { history ->
                        completionMap.clear()
                        history.forEach { h ->
                            runCatching {
                                val d = LocalDate.parse(h.date, dateFmt)
                                val ratio = if (h.totalScheduled > 0)
                                    h.totalCompleted.toFloat() / h.totalScheduled else 0f
                                completionMap[d] = ratio
                            }
                        }
                        renderMonth()
                        buildDowHeader()
                    }
                }

                launch {
                    viewModel.selectedDateEntries.collect { entries ->
                        entryAdapter.submitList(entries)
                        binding.tvSelectedDate.text = selectedDate.format(
                            DateTimeFormatter.ofPattern("EEE, MMM d yyyy"))
                        binding.tvEntryCount.text = "${entries.size} entries"
                        binding.layoutDayDetail.visibility =
                            if (entries.isNotEmpty()) View.VISIBLE else View.GONE
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
