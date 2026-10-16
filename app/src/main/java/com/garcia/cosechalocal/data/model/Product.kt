package com.garcia.cosechalocal.data.model

import java.io.Serializable

data class Product(
    val id: String,
    val name: String,
    val producer: String,
    val distanceKm: Double,
    val price: Int,
    val stock: Int,
    val category: String,
    val description: String
) : Serializable
