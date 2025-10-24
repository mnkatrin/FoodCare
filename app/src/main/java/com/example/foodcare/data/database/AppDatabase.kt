package com.example.foodcare.data.database

import android.content.Context
import androidx.room.*
import com.example.foodcare.data.dao.ProductDao
import com.example.foodcare.data.model.Product

@Database(
    entities = [Product::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "foodcare_database"
                ).fallbackToDestructiveMigration() // Простое решение - удаляет старую БД
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}