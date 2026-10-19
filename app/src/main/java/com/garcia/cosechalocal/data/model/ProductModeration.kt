package com.garcia.cosechalocal.data.model

import java.io.Serializable

enum class ModerationStatus { POR_APROBAR, APROBADO, SUSPENDIDO }

data class ProductModeration(
    val product: Product,
    val status: ModerationStatus,
    val sellerName: String
) : Serializable
