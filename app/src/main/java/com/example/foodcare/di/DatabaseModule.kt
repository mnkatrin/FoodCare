// app/src/main/java/com/example/foodcare/di/DatabaseModule.kt
package com.example.foodcare.di // <-- ВАЖНО: именно этот пакет

import android.content.Context
import androidx.room.Room
import com.example.foodcare.data.database.AppDatabase
import com.example.foodcare.data.dao.ProductDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // Указываем, что зависимости будут singleton
object DatabaseModule {

    @Provides
    @Singleton // Указываем, что база данных singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "foodcare_database"
        ).fallbackToDestructiveMigration() // Или используйте миграции
            .build()
    }

    @Provides
    @Singleton // Указываем, что DAO singleton
    fun provideProductDao(database: AppDatabase): ProductDao {
        return database.productDao()
    }
}