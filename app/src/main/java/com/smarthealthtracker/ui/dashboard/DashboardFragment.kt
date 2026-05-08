package com.smarthealthtracker.ui.dashboard

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
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.smarthealthtracker.R
import com.smarthealthtracker.databinding.FragmentDashboardBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@AndroidEntryPoint
class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DashboardViewModel by viewModels()
    private lateinit var entryAdapter: HealthEntryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupHeader()
        setupRecyclerView()
        setupFab()
        observeState()
    }

    private fun setupHeader() {
        val today = LocalDate.now()
        binding.tvDate.text = today.format(
            DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.getDefault())
        )
    }

    private fun setupRecyclerView() {
        entryAdapter = HealthEntryAdapter(
            onComplete = { entry -> viewModel.completeEntry(entry.id) },
            onEdit = { entry ->
                findNavController().navigate(
                    DashboardFragmentDirections.actionDashboardToAddEditEntry(entry.id)
                )
            },
            onDelete = { entry -> viewModel.deleteEntry(entry) },
            onAlarm = { entry -> viewModel.scheduleAlarmForEntry(entry) }
        )

        binding.recyclerEntries.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = entryAdapter
            setHasFixedSize(false)
        }
    }

    private fun setupFab() {
        binding.fabAddEntry.setOnClickListener {
            findNavController().navigate(
                DashboardFragmentDirections.actionDashboardToAddEditEntry(-1L)
            )
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    viewModel.uiState.collect { state ->
                        binding.progressBar.visibility =
                            if (state.isLoading) View.VISIBLE else View.GONE

                        entryAdapter.submitList(state.entries)

                        // Progress ring
                        val progress = if (state.totalCount > 0)
                            (state.completedCount * 100 / state.totalCount) else 0
                        binding.progressCircular.progress = progress
                        binding.tvProgress.text = "$progress%"
                        binding.tvProgressLabel.text =
                            "${state.completedCount} / ${state.totalCount} done"

                        // Streak
                        binding.tvStreakCount.text = "${state.currentStreak}"
                        binding.tvStreakLabel.text = if (state.currentStreak == 1)
                            "day streak 🔥" else "days streak 🔥"

                        // Empty state
                        binding.layoutEmptyState.visibility =
                            if (state.entries.isEmpty() && !state.isLoading)
                                View.VISIBLE else View.GONE
                    }
                }

                launch {
                    viewModel.message.collect { msg ->
                        Snackbar.make(binding.root, msg, Snackbar.LENGTH_SHORT).show()
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
