package com.garcia.cosechalocal.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "product_images")
data class ProductImageEntity(
    @PrimaryKey val productId: String,
    val imageData: ByteArray,
    val mimeType: String = "image/jpeg",
    val updatedAt: Long = System.currentTimeMillis()
)
