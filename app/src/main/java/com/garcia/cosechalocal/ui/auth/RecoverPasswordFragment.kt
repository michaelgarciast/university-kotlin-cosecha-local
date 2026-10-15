package com.garcia.cosechalocal.ui.auth

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.garcia.cosechalocal.R
import com.garcia.cosechalocal.databinding.FragmentRecoverPasswordBinding
import com.google.android.material.snackbar.Snackbar

class RecoverPasswordFragment : Fragment() {

    private var _binding: FragmentRecoverPasswordBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecoverPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSendLink.setOnClickListener {
            if (validateEmail()) {
                Snackbar.make(
                    requireView(),
                    getString(R.string.recover_link_sent),
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }

        binding.tvBackToLoginRecover.setOnClickListener {
            findNavController().navigate(R.id.action_recoverPasswordFragment_to_loginFragment)
        }
    }

    private fun validateEmail(): Boolean {
        val email = binding.etRecoverEmail.text?.toString()?.trim().orEmpty()
        return if (email.isEmpty()) {
            binding.tilRecoverEmail.error = getString(R.string.error_empty_email)
            false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilRecoverEmail.error = getString(R.string.error_invalid_email)
            false
        } else {
            binding.tilRecoverEmail.error = null
            true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
