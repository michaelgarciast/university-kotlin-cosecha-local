package com.garcia.cosechalocal.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ProductImageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(image: ProductImageEntity)

    @Query("SELECT * FROM product_images WHERE productId = :productId")
    suspend fun getByProductId(productId: String): ProductImageEntity?

    @Query("DELETE FROM product_images WHERE productId = :productId")
    suspend fun deleteByProductId(productId: String)
}
