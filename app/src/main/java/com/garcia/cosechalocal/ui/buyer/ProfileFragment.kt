package com.garcia.cosechalocal.ui.buyer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.garcia.cosechalocal.R
import com.garcia.cosechalocal.databinding.FragmentProfileBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()
    }

    private fun setupListeners() {
        binding.switchBiometrics.setOnCheckedChangeListener { _, isChecked ->
            val message = if (isChecked) {
                getString(R.string.biometrics_enabled)
            } else {
                getString(R.string.biometrics_disabled)
            }
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }

        binding.btnEditProfile.setOnClickListener {
            Toast.makeText(
                requireContext(),
                getString(R.string.edit_profile_simulated),
                Toast.LENGTH_SHORT
            ).show()
        }

        binding.btnLogout.setOnClickListener {
            performLogout()
        }

        binding.btnDeleteAccount.setOnClickListener {
            showDeleteAccountConfirmation()
        }
    }

    private fun performLogout() {
        findNavController().navigate(
            R.id.loginFragment,
            null,
            androidx.navigation.NavOptions.Builder()
                .setPopUpTo(R.id.nav_graph, true)
                .build()
        )
    }

    private fun showDeleteAccountConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.delete_account_dialog_title))
            .setMessage(getString(R.string.delete_account_dialog_msg))
            .setPositiveButton(getString(R.string.btn_confirm_delete)) { dialog, _ ->
                dialog.dismiss()
                performLogout()
            }
            .setNegativeButton(getString(R.string.btn_cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
