package com.garcia.cosechalocal.data.model

import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import com.garcia.cosechalocal.R
import java.io.Serializable

enum class ModerationStatus(
    @param:StringRes val labelRes: Int,
    @param:ColorRes val bgColorRes: Int,
    @param:ColorRes val textColorRes: Int
) {
    POR_APROBAR(R.string.product_status_pending, R.color.status_pending_bg, R.color.status_pending_text),
    APROBADO(R.string.product_status_approved, R.color.status_delivered_bg, R.color.status_delivered_text),
    SUSPENDIDO(R.string.product_status_suspended, R.color.stock_low_bg, R.color.error_red)
}

data class ProductModeration(
    val product: Product,
    val status: ModerationStatus,
    val sellerName: String
) : Serializable
