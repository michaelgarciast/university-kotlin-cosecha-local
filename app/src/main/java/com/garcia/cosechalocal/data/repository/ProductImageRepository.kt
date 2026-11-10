package com.garcia.cosechalocal.data.repository

import android.content.Context
import com.garcia.cosechalocal.data.local.CosechaLocalDatabase
import com.garcia.cosechalocal.data.local.ProductImageEntity

class ProductImageRepository(context: Context) {

    private val productImageDao = CosechaLocalDatabase
        .getInstance(context)
        .productImageDao()

    suspend fun saveImage(productId: String, imageData: ByteArray, mimeType: String) {
        productImageDao.upsert(
            ProductImageEntity(
                productId = productId,
                imageData = imageData,
                mimeType = mimeType
            )
        )
    }

    suspend fun getImage(productId: String): ProductImageEntity? =
        productImageDao.getByProductId(productId)

    suspend fun deleteImage(productId: String) {
        productImageDao.deleteByProductId(productId)
    }
}
