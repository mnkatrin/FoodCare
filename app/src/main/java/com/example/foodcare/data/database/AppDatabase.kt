package com.example.foodcare.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.foodcare.data.dao.HistoryDao
import com.example.foodcare.data.dao.ProductDao
import com.example.foodcare.data.model.Category
import com.example.foodcare.data.model.HistoryEvent
import com.example.foodcare.data.model.Product

@Database(
    entities = [
        Product::class,
        Category::class,
        HistoryEvent::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao
    abstract fun historyDao(): HistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "foodcare_database"
                )
                    // ВАЖНО: эта строка ОБЯЗАТЕЛЬНО должна быть
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
