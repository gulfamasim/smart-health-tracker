package com.smarthealthtracker.ui.settings

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.smarthealthtracker.databinding.FragmentSettingsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SettingsViewModel by viewModels()

    private val createFileLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? -> uri?.let { viewModel.exportBackup(requireContext(), it) } }

    private val openFileLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? -> uri?.let { viewModel.importBackup(requireContext(), it) } }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupDarkModeSwitch()
        setupBackupButtons()
        setupClearDataButton()
        observeMessages()
    }

    private fun setupDarkModeSwitch() {
        // Read current mode from shared prefs
        val prefs = requireContext().getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
        val isDark = prefs.getBoolean("dark_mode", true) // default dark
        binding.switchDarkMode.isChecked = isDark

        binding.switchDarkMode.setOnCheckedChangeListener { _, checked ->
            // Save preference
            prefs.edit().putBoolean("dark_mode", checked).apply()

            // Apply immediately
            AppCompatDelegate.setDefaultNightMode(
                if (checked) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }
    }

    private fun setupBackupButtons() {
        binding.btnExportBackup.setOnClickListener {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
            createFileLauncher.launch("health_backup_$timestamp.json")
        }
        binding.btnImportBackup.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Import Backup")
                .setMessage("This will add data from the backup file. Continue?")
                .setPositiveButton("Import") { _, _ ->
                    openFileLauncher.launch(arrayOf("application/json"))
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun setupClearDataButton() {
        binding.btnClearData.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Clear All Data")
                .setMessage("Permanently delete all entries and history? Schedules will be kept.")
                .setPositiveButton("Clear") { _, _ -> viewModel.clearAllData() }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun observeMessages() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.message.collect { msg ->
                    Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
