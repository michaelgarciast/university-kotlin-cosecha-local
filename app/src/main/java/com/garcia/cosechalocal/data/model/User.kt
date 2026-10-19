package com.garcia.cosechalocal.data.model

import com.garcia.cosechalocal.ui.common.UserRole
import java.io.Serializable

data class User(
    val id: Int,
    val name: String,
    val email: String,
    val role: UserRole,
    val active: Boolean
) : Serializable
