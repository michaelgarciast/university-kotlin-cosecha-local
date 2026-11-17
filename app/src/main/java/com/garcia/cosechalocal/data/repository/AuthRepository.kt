package com.garcia.cosechalocal.data.repository

import com.garcia.cosechalocal.data.model.UserProfile
import com.garcia.cosechalocal.ui.common.UserRole
import com.garcia.cosechalocal.utils.BiometricLock
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class AccountDisabledException : Exception()
class MissingProfileException : Exception()
class InvalidRoleException : Exception()

object AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    val currentUser get() = auth.currentUser

    fun signOut() {
        BiometricLock.disable()
        auth.signOut()
    }

    fun register(
        name: String,
        email: String,
        password: String,
        role: UserRole,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val user = result.user
                if (user == null) {
                    onError(MissingProfileException())
                    return@addOnSuccessListener
                }

                val profile = hashMapOf(
                    "name" to name,
                    "email" to email,
                    "role" to role.name,
                    "active" to true,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )

                firestore.collection("users").document(user.uid).set(profile)
                    .addOnSuccessListener {
                        signOut()
                        onSuccess()
                    }
                    .addOnFailureListener { error ->
                        user.delete().addOnCompleteListener {
                            signOut()
                            onError(error)
                        }
                    }
            }
            .addOnFailureListener(onError)
    }

    fun signIn(
        email: String,
        password: String,
        onSuccess: (UserProfile) -> Unit,
        onError: (Exception) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid
                if (uid == null) {
                    onError(MissingProfileException())
                    return@addOnSuccessListener
                }
                if (!BiometricLock.isEnabledFor(uid)) BiometricLock.disable()
                loadProfile(uid, onSuccess, onError)
            }
            .addOnFailureListener(onError)
    }

    fun loadCurrentProfile(
        onSuccess: (UserProfile) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            onError(MissingProfileException())
            return
        }
        loadProfile(uid, onSuccess, onError)
    }

    fun sendPasswordReset(
        email: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener(onError)
    }

    private fun loadProfile(
        uid: String,
        onSuccess: (UserProfile) -> Unit,
        onError: (Exception) -> Unit
    ) {
        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    signOut()
                    onError(MissingProfileException())
                    return@addOnSuccessListener
                }

                val role = runCatching {
                    UserRole.valueOf(document.getString("role").orEmpty())
                }.getOrNull()

                if (role == null) {
                    signOut()
                    onError(InvalidRoleException())
                    return@addOnSuccessListener
                }

                if (document.getBoolean("active") != true) {
                    signOut()
                    onError(AccountDisabledException())
                    return@addOnSuccessListener
                }

                onSuccess(
                    UserProfile(
                        uid = uid,
                        name = document.getString("name").orEmpty(),
                        email = document.getString("email").orEmpty(),
                        role = role,
                        active = true
                    )
                )
            }
            .addOnFailureListener { error ->
                signOut()
                onError(error)
            }
    }
}
