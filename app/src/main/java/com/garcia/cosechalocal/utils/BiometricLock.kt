package com.garcia.cosechalocal.utils

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import com.google.firebase.FirebaseApp
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

// Bloqueo biométrico de la sesión de Firebase. La huella no genera un login: solo
// desbloquea una sesión ya válida. Una clave de Android Keystore exige biometría
// fuerte en cada uso y se invalida si cambian las huellas inscritas. La
// preferencia guarda el UID para el que se activó y se borra al cerrar sesión.
object BiometricLock {

    private const val KEY_ALIAS = "cosecha_biometric_lock"
    private const val PREFS = "biometric_lock"
    private const val PREF_UID = "uid"
    private const val AUTHENTICATORS = BiometricManager.Authenticators.BIOMETRIC_STRONG

    private val context get() = FirebaseApp.getInstance().applicationContext
    private val prefs get() = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun isAvailable(context: Context): Boolean =
        BiometricManager.from(context).canAuthenticate(AUTHENTICATORS) == BiometricManager.BIOMETRIC_SUCCESS

    fun isEnabledFor(uid: String?): Boolean = uid != null && prefs.getString(PREF_UID, null) == uid

    fun enable(uid: String) {
        generateKey()
        prefs.edit().putString(PREF_UID, uid).apply()
    }

    fun disable() {
        prefs.edit().remove(PREF_UID).apply()
        runCatching { keyStore().deleteEntry(KEY_ALIAS) }
    }

    // Lanza excepción si la clave no existe o quedó invalidada por un cambio de huellas.
    fun createCipher(): Cipher = Cipher.getInstance("AES/GCM/NoPadding").apply {
        init(Cipher.ENCRYPT_MODE, keyStore().getKey(KEY_ALIAS, null) as SecretKey)
    }

    fun promptInfo(title: String, subtitle: String, cancel: String): BiometricPrompt.PromptInfo =
        BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText(cancel)
            .setAllowedAuthenticators(AUTHENTICATORS)
            .build()

    // Comprueba que la clave protegida se desbloqueó de verdad con la huella.
    fun verify(result: BiometricPrompt.AuthenticationResult): Boolean =
        runCatching { result.cryptoObject?.cipher?.doFinal(ByteArray(1)) != null }.getOrDefault(false)

    private fun keyStore() = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

    private fun generateKey() {
        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setUserAuthenticationRequired(true)
            .setInvalidatedByBiometricEnrollment(true)
            .build()
        KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            .apply { init(spec) }
            .generateKey()
    }
}
