package com.smarthealthtracker.ui.statistics

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.smarthealthtracker.R
import com.smarthealthtracker.databinding.FragmentStatisticsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@AndroidEntryPoint
class StatisticsFragment : Fragment() {

    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: StatisticsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCharts()
        observeState()
    }

    private fun setupCharts() {
        // Bar chart style
        binding.barChart.apply {
            description.isEnabled = false
            legend.isEnabled = true
            setDrawGridBackground(false)
            setDrawBarShadow(false)
            setScaleEnabled(false)
            setPinchZoom(false)
            axisRight.isEnabled = false
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.granularity = 1f
            xAxis.setDrawGridLines(false)
            axisLeft.granularity = 1f
            axisLeft.axisMinimum = 0f
            animateY(800)
        }

        // Pie chart style
        binding.pieChart.apply {
            description.isEnabled = false
            setUsePercentValues(true)
            setDrawEntryLabels(true)
            setEntryLabelColor(Color.WHITE)
            legend.isEnabled = true
            animateY(800)
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    if (state.isLoading) return@collect

                    // Summary cards
                    binding.tvCurrentStreak.text = "${state.currentStreak} 🔥"
                    binding.tvMaxStreak.text = "${state.maxStreak}"
                    binding.tvWeeklyRate.text =
                        "${(state.weeklyCompletion * 100).roundToInt()}%"
                    binding.tvTotalCompleted.text = "${state.totalCompleted}"
                    binding.tvTotalMissed.text = "${state.totalMissed}"

                    // Streak label
                    binding.tvStreakLabel.text = when {
                        state.currentStreak == 0 -> "Start your streak today!"
                        state.currentStreak < 3  -> "Great start! Keep going 💪"
                        state.currentStreak < 7  -> "Building momentum! 🚀"
                        state.currentStreak < 14 -> "One week strong! 🌟"
                        else                     -> "Amazing consistency! 🏆"
                    }

                    // Bar chart: last 7 days completed vs total
                    val last7 = state.recentHistory.takeLast(7)
                    if (last7.isNotEmpty()) {
                        val completedEntries = last7.mapIndexed { i, h ->
                            BarEntry(i.toFloat(), h.totalCompleted.toFloat())
                        }
                        val missedEntries = last7.mapIndexed { i, h ->
                            BarEntry(i.toFloat(), h.totalMissed.toFloat())
                        }

                        val completedSet = BarDataSet(completedEntries, "Completed").apply {
                            color = ContextCompat.getColor(
                                requireContext(), R.color.streak_green)
                            valueTextColor = Color.GRAY
                        }
                        val missedSet = BarDataSet(missedEntries, "Missed").apply {
                            color = ContextCompat.getColor(
                                requireContext(), R.color.missed_red)
                            valueTextColor = Color.GRAY
                        }

                        val labels = last7.map { h ->
                            try {
                                LocalDate.parse(h.date)
                                    .format(DateTimeFormatter.ofPattern("EEE"))
                            } catch (_: Exception) { h.date }
                        }

                        binding.barChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
                        binding.barChart.data = BarData(completedSet, missedSet).apply {
                            barWidth = 0.3f
                        }
                        (binding.barChart.data as BarData).groupBars(0f, 0.08f, 0.02f)
                        binding.barChart.invalidate()
                    }

                    // Pie chart: completed vs missed overall
                    val total = state.totalCompleted + state.totalMissed
                    if (total > 0) {
                        val pieEntries = listOf(
                            PieEntry(state.totalCompleted.toFloat(), "Completed"),
                            PieEntry(state.totalMissed.toFloat(), "Missed")
                        )
                        val pieSet = PieDataSet(pieEntries, "").apply {
                            colors = listOf(
                                ContextCompat.getColor(requireContext(), R.color.streak_green),
                                ContextCompat.getColor(requireContext(), R.color.missed_red)
                            )
                            sliceSpace = 2f
                            valueTextSize = 14f
                            valueTextColor = Color.WHITE
                        }
                        binding.pieChart.data = PieData(pieSet)
                        binding.pieChart.invalidate()
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
