package com.garcia.cosechalocal.ui.auth

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.garcia.cosechalocal.R
import com.garcia.cosechalocal.data.repository.AuthRepository
import com.garcia.cosechalocal.databinding.FragmentRegisterBinding
import com.garcia.cosechalocal.ui.common.UserRole
import com.google.firebase.auth.FirebaseAuthUserCollisionException

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnRegister.setOnClickListener {
            if (validateRegistration()) {
                performRegistration()
            }
        }

        binding.tvBackToLogin.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }
    }

    private fun performRegistration() {
        val fullName = binding.etFullName.text?.toString()?.trim().orEmpty()
        val email = binding.etRegisterEmail.text?.toString()?.trim().orEmpty()
        val password = binding.etRegisterPassword.text?.toString()?.trim().orEmpty()
        val role = if (binding.rbSeller.isChecked) UserRole.SELLER else UserRole.BUYER

        setLoading(true)
        AuthRepository.register(
            name = fullName,
            email = email,
            password = password,
            role = role,
            onSuccess = {
                if (!isAdded) return@register
                Toast.makeText(
                    requireContext(),
                    getString(R.string.register_success),
                    Toast.LENGTH_LONG
                ).show()
                findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
            },
            onError = { error ->
                if (!isAdded) return@register
                setLoading(false)
                val message = if (error is FirebaseAuthUserCollisionException) {
                    getString(R.string.error_email_duplicate)
                } else {
                    getString(R.string.error_register_failed)
                }
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            }
        )
    }

    private fun setLoading(loading: Boolean) {
        val currentBinding = _binding ?: return
        currentBinding.btnRegister.isEnabled = !loading
        currentBinding.btnRegister.alpha = if (loading) 0.6f else 1f
    }

    private fun validateRegistration(): Boolean {
        var isValid = true

        val fullName = binding.etFullName.text?.toString()?.trim().orEmpty()
        val email = binding.etRegisterEmail.text?.toString()?.trim().orEmpty()
        val password = binding.etRegisterPassword.text?.toString()?.trim().orEmpty()
        val confirmPassword = binding.etConfirmPassword.text?.toString()?.trim().orEmpty()
        val termsAccepted = binding.cbAcceptTerms.isChecked

        if (fullName.isEmpty()) {
            binding.tilFullName.error = getString(R.string.error_empty_name)
            isValid = false
        } else {
            binding.tilFullName.error = null
        }

        if (email.isEmpty()) {
            binding.tilRegisterEmail.error = getString(R.string.error_empty_email)
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilRegisterEmail.error = getString(R.string.error_invalid_email)
            isValid = false
        } else {
            binding.tilRegisterEmail.error = null
        }

        if (password.isEmpty()) {
            binding.tilRegisterPassword.error = getString(R.string.error_empty_password)
            isValid = false
        } else if (password.length < 6) {
            binding.tilRegisterPassword.error = getString(R.string.error_password_length)
            isValid = false
        } else {
            binding.tilRegisterPassword.error = null
        }

        if (confirmPassword.isEmpty() || confirmPassword != password) {
            binding.tilConfirmPassword.error = getString(R.string.error_password_mismatch)
            isValid = false
        } else {
            binding.tilConfirmPassword.error = null
        }

        if (!termsAccepted) {
            Toast.makeText(requireContext(), getString(R.string.error_accept_terms), Toast.LENGTH_SHORT).show()
            isValid = false
        }

        return isValid
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
