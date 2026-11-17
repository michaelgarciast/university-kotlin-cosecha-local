package com.garcia.cosechalocal.ui.common

import android.content.Intent
import androidx.fragment.app.Fragment
import com.garcia.cosechalocal.data.repository.AuthRepository
import com.garcia.cosechalocal.ui.main.MainActivity
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
