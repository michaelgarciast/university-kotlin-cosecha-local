package com.garcia.cosechalocal.ui.common

import androidx.annotation.StringRes
import com.garcia.cosechalocal.R

enum class UserRole(@param:StringRes val labelRes: Int) {
    BUYER(R.string.role_buyer),
    SELLER(R.string.role_seller),
    ADMIN(R.string.role_admin)
}
