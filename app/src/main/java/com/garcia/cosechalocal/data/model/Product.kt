package com.garcia.cosechalocal.data.model

import java.io.Serializable

data class Product(
    val id: String,
    val sellerId: String,
    val name: String,
    val producer: String,
    val distanceKm: Double,
    val price: Int,
    val stock: Int,
    val category: String,
    val description: String,
    val hasLocalImage: Boolean,
    val status: ModerationStatus
) : Serializable
