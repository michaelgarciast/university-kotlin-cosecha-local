package com.garcia.cosechalocal.ui.admin

import com.garcia.cosechalocal.R
import com.garcia.cosechalocal.ui.common.UserRole

val UserRole.bgColorRes: Int
    get() = when (this) {
        UserRole.BUYER -> R.color.chip_selected_bg
        UserRole.SELLER -> R.color.status_pending_bg
        UserRole.ADMIN -> R.color.status_shipped_bg
    }

val UserRole.textColorRes: Int
    get() = when (this) {
        UserRole.BUYER -> R.color.primary_green
        UserRole.SELLER -> R.color.status_pending_text
        UserRole.ADMIN -> R.color.status_shipped_text
    }


