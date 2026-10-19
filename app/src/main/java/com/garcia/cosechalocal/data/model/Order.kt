package com.garcia.cosechalocal.data.model

import java.io.Serializable

enum class OrderStatus { PENDIENTE, PREPARANDO, ENVIADO, ENTREGADO }

data class Order(
    val id: Int,
    val buyerName: String,
    val itemsSummary: String,
    val total: Int,
    val status: OrderStatus,
    val date: String
) : Serializable
