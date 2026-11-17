package com.garcia.cosechalocal.ui.common

import android.content.Intent
import android.widget.CompoundButton
import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.garcia.cosechalocal.data.repository.AuthRepository
import com.garcia.cosechalocal.ui.main.MainActivity
import com.garcia.cosechalocal.R
import com.garcia.cosechalocal.utils.BiometricLock
import com.garcia.cosechalocal.utils.ProductImageStore

// Reinicia MainActivity para destruir los ViewModels y listeners de Firestore
// de la sesión anterior; así otra cuenta no hereda datos en memoria.
fun Fragment.logoutToLogin() {
    AuthRepository.signOut()
    ProductImageStore.clearCache()
    val activity = requireActivity()
    activity.startActivity(
        Intent(activity, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    )
    activity.finish()
}

// Activar exige sesión abierta y confirmar la huella con la clave del Keystore;
// desactivar borra la clave. Si se cancela, el interruptor vuelve a su estado.
fun Fragment.setupBiometricSwitch(toggle: CompoundButton) {
    val uid = AuthRepository.currentUser?.uid
    var updating = false
    fun setChecked(value: Boolean) {
        updating = true
        toggle.isChecked = value
        updating = false
    }
    setChecked(BiometricLock.isEnabledFor(uid))

    toggle.setOnCheckedChangeListener { _, checked ->
        if (updating) return@setOnCheckedChangeListener
        val context = requireContext()
        if (!checked) {
            BiometricLock.disable()
            return@setOnCheckedChangeListener
        }
        if (uid == null || !BiometricLock.isAvailable(context)) {
            Toast.makeText(context, getString(R.string.biometric_unavailable), Toast.LENGTH_SHORT).show()
            setChecked(false)
            return@setOnCheckedChangeListener
        }
        val cipher = runCatching {
            BiometricLock.enable(uid)
            BiometricLock.createCipher()
        }.getOrElse {
            BiometricLock.disable()
            Toast.makeText(context, getString(R.string.biometric_error), Toast.LENGTH_SHORT).show()
            setChecked(false)
            return@setOnCheckedChangeListener
        }
        val info = BiometricLock.promptInfo(
            getString(R.string.biometric_prompt_title),
            getString(R.string.biometric_enable_subtitle),
            getString(R.string.biometric_prompt_cancel)
        )
        BiometricPrompt(this, ContextCompat.getMainExecutor(context),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    if (!BiometricLock.verify(result)) fail()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) = fail()

                private fun fail() {
                    BiometricLock.disable()
                    if (isAdded) setChecked(false)
                }
            }).authenticate(info, BiometricPrompt.CryptoObject(cipher))
    }
}
