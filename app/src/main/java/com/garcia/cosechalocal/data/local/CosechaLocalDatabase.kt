package com.garcia.cosechalocal.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ProductImageEntity::class],
    version = 1,
    exportSchema = false
)
abstract class CosechaLocalDatabase : RoomDatabase() {

    abstract fun productImageDao(): ProductImageDao

    companion object {
        @Volatile
        private var instance: CosechaLocalDatabase? = null

        fun getInstance(context: Context): CosechaLocalDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    CosechaLocalDatabase::class.java,
                    "cosecha_local.db"
                ).build().also { instance = it }
            }
    }
}
