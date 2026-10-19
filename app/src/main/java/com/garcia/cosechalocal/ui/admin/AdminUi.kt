package com.garcia.cosechalocal.ui.admin

import com.garcia.cosechalocal.R
import com.garcia.cosechalocal.data.model.ModerationStatus
import com.garcia.cosechalocal.ui.common.UserRole

val UserRole.labelRes: Int
    get() = when (this) {
        UserRole.BUYER -> R.string.role_buyer
        UserRole.SELLER -> R.string.role_seller
        UserRole.ADMIN -> R.string.role_admin
    }

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

val ModerationStatus.labelRes: Int
    get() = when (this) {
        ModerationStatus.POR_APROBAR -> R.string.product_status_pending
        ModerationStatus.APROBADO -> R.string.product_status_approved
        ModerationStatus.SUSPENDIDO -> R.string.product_status_suspended
    }

val ModerationStatus.bgColorRes: Int
    get() = when (this) {
        ModerationStatus.POR_APROBAR -> R.color.status_pending_bg
        ModerationStatus.APROBADO -> R.color.status_delivered_bg
        ModerationStatus.SUSPENDIDO -> R.color.stock_low_bg
    }

val ModerationStatus.textColorRes: Int
    get() = when (this) {
        ModerationStatus.POR_APROBAR -> R.color.status_pending_text
        ModerationStatus.APROBADO -> R.color.status_delivered_text
        ModerationStatus.SUSPENDIDO -> R.color.error_red
    }
