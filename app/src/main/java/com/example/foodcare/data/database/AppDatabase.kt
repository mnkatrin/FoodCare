package com.example.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.example.data.database.dao.ProductDao
import com.example.data.database.dao.UserDao
import com.example.data.database.entities.User
import com.example.data.database.entities.UserProduct
import com.example.data.database.entities.UserSettings

@Database(
    entities = [
        User::class,
        UserProduct::class,
        UserSettings::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun productDao(): ProductDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "food_expiry_app.db"
                )
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}