package com.garcia.cosechalocal.ui.auth

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.garcia.cosechalocal.R
import com.garcia.cosechalocal.data.repository.AccountDisabledException
import com.garcia.cosechalocal.data.repository.AuthRepository
import com.garcia.cosechalocal.data.repository.InvalidRoleException
import com.garcia.cosechalocal.data.repository.MissingProfileException
import com.garcia.cosechalocal.data.model.UserProfile
import com.garcia.cosechalocal.databinding.FragmentLoginBinding
import com.garcia.cosechalocal.ui.common.UserRole
import com.garcia.cosechalocal.ui.main.MainActivity
import com.garcia.cosechalocal.utils.BiometricLock

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnLogin.setOnClickListener {
            if (validateInputs()) {
                performLogin()
            }
        }

        binding.btnFingerprint.setOnClickListener {
            showBiometricPrompt()
        }

        binding.tvForgotPassword.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_recoverPasswordFragment)
        }

        binding.tvRegisterLink.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        updateBiometricButton()
        if (AuthRepository.currentUser != null) {
            if (BiometricLock.isEnabledFor(AuthRepository.currentUser?.uid)) showBiometricPrompt()
            else loadCurrentProfile()
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true
        val email = binding.etEmail.text?.toString()?.trim().orEmpty()
        val password = binding.etPassword.text?.toString()?.trim().orEmpty()

        if (email.isEmpty()) {
            binding.tilEmail.error = getString(R.string.error_empty_email)
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = getString(R.string.error_invalid_email)
            isValid = false
        } else {
            binding.tilEmail.error = null
        }

        if (password.isEmpty()) {
            binding.tilPassword.error = getString(R.string.error_empty_password)
            isValid = false
        } else if (password.length < 6) {
            binding.tilPassword.error = getString(R.string.error_password_length)
            isValid = false
        } else {
            binding.tilPassword.error = null
        }

        return isValid
    }

    private fun performLogin() {
        val email = binding.etEmail.text?.toString()?.trim().orEmpty()
        val password = binding.etPassword.text?.toString()?.trim().orEmpty()

        setLoading(true)
        AuthRepository.signIn(
            email = email,
            password = password,
            onSuccess = { profile -> onSignedIn(profile) },
            onError = { error ->
                if (!isAdded) return@signIn
                setLoading(false)
                showAuthError(error)
            }
        )
    }

    // El selector solo exige coherencia: el rol real siempre viene del perfil
    // en Firestore, nunca de la interfaz.
    private fun onSignedIn(profile: UserProfile) {
        if (!isAdded) return
        if (profile.role != selectedRole()) {
            AuthRepository.signOut()
            setLoading(false)
            Toast.makeText(
                requireContext(),
                getString(R.string.error_role_mismatch, getString(profile.role.labelRes)),
                Toast.LENGTH_LONG
            ).show()
            return
        }
        navigateToRole(profile.role)
    }

    private fun selectedRole(): UserRole = when (binding.toggleRole.checkedButtonId) {
        R.id.btnRoleSeller -> UserRole.SELLER
        R.id.btnRoleAdmin -> UserRole.ADMIN
        else -> UserRole.BUYER
    }

    private fun loadCurrentProfile() {
        setLoading(true)
        AuthRepository.loadCurrentProfile(
            onSuccess = { profile -> navigateToRole(profile.role) },
            onError = { error ->
                if (!isAdded) return@loadCurrentProfile
                setLoading(false)
                showAuthError(error)
            }
        )
    }

    private fun navigateToRole(role: UserRole) {
        if (!isAdded) return
        (activity as? MainActivity)?.setupRoleMenu(role)
    }

    private fun setLoading(loading: Boolean) {
        val currentBinding = _binding ?: return
        currentBinding.btnLogin.isEnabled = !loading
        currentBinding.btnFingerprint.isEnabled = !loading
        currentBinding.toggleRole.isEnabled = !loading
        currentBinding.btnLogin.alpha = if (loading) 0.6f else 1f
    }

    private fun showAuthError(error: Exception) {
        val message = when (error) {
            is AccountDisabledException -> getString(R.string.error_account_disabled)
            is MissingProfileException -> getString(R.string.error_profile_missing)
            is InvalidRoleException -> getString(R.string.error_role_invalid)
            else -> getString(R.string.error_login_failed)
        }
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    // Con huella activada la sesión guardada no se restaura sola: hace falta
    // desbloquearla. Cancelar o fallar deja al usuario en el login sin entrar.
    private fun showBiometricPrompt() {
        val context = requireContext()
        if (AuthRepository.currentUser == null || !BiometricLock.isAvailable(context)) {
            updateBiometricButton()
            Toast.makeText(context, getString(R.string.biometric_error), Toast.LENGTH_SHORT).show()
            return
        }

        val cipher = runCatching { BiometricLock.createCipher() }.getOrElse {
            AuthRepository.signOut()
            updateBiometricButton()
            Toast.makeText(context, getString(R.string.biometric_invalidated), Toast.LENGTH_LONG).show()
            return
        }

        val promptInfo = BiometricLock.promptInfo(
            getString(R.string.biometric_prompt_title),
            getString(R.string.biometric_prompt_subtitle),
            getString(R.string.biometric_prompt_cancel)
        )

        val biometricPrompt = BiometricPrompt(this, ContextCompat.getMainExecutor(context),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    if (BiometricLock.verify(result)) loadCurrentProfile() else showBiometricFailure()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    if (isAdded && errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                        errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON &&
                        errorCode != BiometricPrompt.ERROR_CANCELED
                    ) {
                        Toast.makeText(requireContext(), errString, Toast.LENGTH_SHORT).show()
                    }
                }
            })

        biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
    }

    private fun showBiometricFailure() {
        if (isAdded) Toast.makeText(requireContext(), getString(R.string.biometric_error), Toast.LENGTH_SHORT).show()
    }

    private fun updateBiometricButton() {
        _binding?.btnFingerprint?.visibility =
            if (BiometricLock.isEnabledFor(AuthRepository.currentUser?.uid)) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
