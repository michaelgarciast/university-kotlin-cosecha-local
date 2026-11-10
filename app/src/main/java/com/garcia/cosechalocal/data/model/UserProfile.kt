package com.garcia.cosechalocal.data.model

import com.garcia.cosechalocal.ui.common.UserRole

data class UserProfile(
    val uid: String,
    val name: String,
    val email: String,
    val role: UserRole,
    val active: Boolean
)
