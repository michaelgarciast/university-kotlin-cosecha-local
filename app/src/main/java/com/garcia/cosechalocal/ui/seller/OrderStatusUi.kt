package com.garcia.cosechalocal.ui.seller

import com.garcia.cosechalocal.R
import com.garcia.cosechalocal.data.model.OrderStatus

val OrderStatus.labelRes: Int
    get() = when (this) {
        OrderStatus.PENDIENTE -> R.string.status_pending
        OrderStatus.PREPARANDO -> R.string.status_preparing
        OrderStatus.ENVIADO -> R.string.status_shipped
        OrderStatus.ENTREGADO -> R.string.status_delivered
    }

val OrderStatus.bgColorRes: Int
    get() = when (this) {
        OrderStatus.PENDIENTE -> R.color.status_pending_bg
        OrderStatus.PREPARANDO -> R.color.status_preparing_bg
        OrderStatus.ENVIADO -> R.color.status_shipped_bg
        OrderStatus.ENTREGADO -> R.color.status_delivered_bg
    }

val OrderStatus.textColorRes: Int
    get() = when (this) {
        OrderStatus.PENDIENTE -> R.color.status_pending_text
        OrderStatus.PREPARANDO -> R.color.status_preparing_text
        OrderStatus.ENVIADO -> R.color.status_shipped_text
        OrderStatus.ENTREGADO -> R.color.status_delivered_text
    }
