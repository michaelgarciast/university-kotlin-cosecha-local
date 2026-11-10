package com.garcia.cosechalocal.data.repository

import com.garcia.cosechalocal.data.model.UserProfile
import com.garcia.cosechalocal.ui.common.UserRole
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.util.UUID

object UserRepository {

    private const val SECONDARY_APP_NAME = "adminUserCreation"

    private val firestore = FirebaseFirestore.getInstance()
    private val users = firestore.collection("users")

    fun observeUsers(
        onChange: (List<UserProfile>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration = users.orderBy("name").addSnapshotListener { snapshot, error ->
        if (error != null) {
            onError(error)
            return@addSnapshotListener
        }
        val list = snapshot?.documents.orEmpty().mapNotNull { doc ->
            val role = runCatching {
                UserRole.valueOf(doc.getString("role").orEmpty())
            }.getOrNull() ?: return@mapNotNull null
            UserProfile(
                uid = doc.id,
                name = doc.getString("name").orEmpty(),
                email = doc.getString("email").orEmpty(),
                role = role,
                active = doc.getBoolean("active") == true
            )
        }
        onChange(list)
    }

    fun createUser(
        name: String,
        email: String,
        role: UserRole,
        active: Boolean,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        // Una app secundaria permite crear la cuenta de Authentication sin
        // cerrar la sesión del administrador, que es lo que haría la instancia
        // principal de FirebaseAuth.
        val defaultApp = FirebaseApp.getInstance()
        val secondaryApp = try {
            FirebaseApp.initializeApp(
                defaultApp.applicationContext,
                defaultApp.options,
                SECONDARY_APP_NAME
            )
        } catch (e: IllegalStateException) {
            FirebaseApp.getInstance(SECONDARY_APP_NAME)
        }
        val secondaryAuth = FirebaseAuth.getInstance(secondaryApp)

        // Contraseña temporal desconocida: el usuario define la suya con el
        // correo de restablecimiento que se envía al terminar.
        val tempPassword = UUID.randomUUID().toString()

        secondaryAuth.createUserWithEmailAndPassword(email, tempPassword)
            .addOnSuccessListener { result ->
                val newUser = result.user
                if (newUser == null) {
                    secondaryApp.delete()
                    onError(IllegalStateException("Firebase devolvió una cuenta vacía"))
                    return@addOnSuccessListener
                }

                val profile = hashMapOf(
                    "name" to name,
                    "email" to email,
                    "role" to role.name,
                    "active" to active,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )

                users.document(newUser.uid).set(profile)
                    .addOnSuccessListener {
                        secondaryAuth.signOut()
                        secondaryApp.delete()
                        // La cuenta ya quedó creada aunque falle este correo.
                        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                            .addOnCompleteListener { onSuccess() }
                    }
                    .addOnFailureListener { error ->
                        newUser.delete().addOnCompleteListener {
                            secondaryAuth.signOut()
                            secondaryApp.delete()
                            onError(error)
                        }
                    }
            }
            .addOnFailureListener { error ->
                secondaryApp.delete()
                onError(error)
            }
    }

    // El correo no se actualiza aquí: cambiar el email de Authentication de
    // otra cuenta requiere el Admin SDK en un backend.
    fun updateUser(
        uid: String,
        name: String,
        role: UserRole,
        active: Boolean,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        users.document(uid).update(
            mapOf(
                "name" to name,
                "role" to role.name,
                "active" to active,
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).addOnSuccessListener { onSuccess() }
            .addOnFailureListener(onError)
    }

    fun setActive(
        uid: String,
        active: Boolean,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        users.document(uid).update(
            mapOf(
                "active" to active,
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).addOnSuccessListener { onSuccess() }
            .addOnFailureListener(onError)
    }

    // Eliminar el perfil bloquea por completo el acceso: sin documento en
    // users/{uid} el login falla y las reglas niegan toda operación. La
    // credencial de Authentication queda huérfana y solo se puede borrar con
    // el Admin SDK en un backend.
    fun deleteUser(
        uid: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        users.document(uid).delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener(onError)
    }
}
