package com.garcia.cosechalocal.utils

import android.content.Context
import com.garcia.cosechalocal.R
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.FirebaseFirestoreException.Code

// Traduce errores a mensajes para el usuario sin exponer detalles internos de
// Firestore. Las validaciones propias (IllegalStateException) ya están en español.
fun Throwable.userMessage(context: Context): String {
    val chain = generateSequence(this) { it.cause }.toList()
    chain.firstOrNull { it is IllegalStateException }?.message?.let { return it }
    if (chain.any { it is FirebaseNetworkException }) return context.getString(R.string.error_network)
    val code = chain.filterIsInstance<FirebaseFirestoreException>().firstOrNull()?.code
    return context.getString(
        when (code) {
            Code.PERMISSION_DENIED -> R.string.error_permission_denied
            Code.UNAVAILABLE, Code.DEADLINE_EXCEEDED -> R.string.error_network
            else -> R.string.error_generic
        }
    )
}
