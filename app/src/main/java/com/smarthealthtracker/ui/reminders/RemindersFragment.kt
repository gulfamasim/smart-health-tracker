package com.smarthealthtracker.ui.reminders

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
import com.smarthealthtracker.R
import com.smarthealthtracker.databinding.FragmentRemindersBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RemindersFragment : Fragment() {

    private var _binding: FragmentRemindersBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RemindersViewModel by viewModels()
    private lateinit var adapter: ScheduleAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRemindersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ScheduleAdapter(
            onToggle = { id, enabled -> viewModel.toggleSchedule(id, enabled) },
            onEdit = { schedule ->
                findNavController().navigate(
                    RemindersFragmentDirections
                        .actionRemindersToAddEditSchedule(schedule.id)
                )
            },
            onDelete = { schedule -> viewModel.deleteSchedule(schedule) }
        )

        binding.recyclerSchedules.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@RemindersFragment.adapter
        }

        binding.fabAddSchedule.setOnClickListener {
            findNavController().navigate(
                RemindersFragmentDirections.actionRemindersToAddEditSchedule(-1L)
            )
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.schedules.collect { schedules ->
                    adapter.submitList(schedules)
                    binding.layoutEmptyState.visibility =
                        if (schedules.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
