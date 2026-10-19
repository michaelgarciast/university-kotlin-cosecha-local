package com.garcia.cosechalocal.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.garcia.cosechalocal.R
import com.garcia.cosechalocal.databinding.FragmentAdminSettingsBinding
import com.google.android.material.snackbar.Snackbar

class AdminSettingsFragment : Fragment() {

    private var _binding: FragmentAdminSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAdminSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapOf(
            binding.rowCategories to R.string.settings_categories,
            binding.rowSecurity to R.string.settings_security,
            binding.rowNotifications to R.string.settings_notifications
        ).forEach { (row, title) ->
            row.setOnClickListener {
                Snackbar.make(binding.root, getString(R.string.settings_option_simulated, getString(title)), Snackbar.LENGTH_SHORT).show()
            }
        }
        binding.btnLogout.setOnClickListener {
            findNavController().navigate(
                R.id.loginFragment,
                null,
                NavOptions.Builder().setPopUpTo(R.id.nav_graph, true).build()
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
