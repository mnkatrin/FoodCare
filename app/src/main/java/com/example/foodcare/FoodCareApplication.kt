package com.example.foodcare

import android.app.Application
import androidx.room.Room // Импорт для Room
import com.example.foodcare.auth.UserManager
import com.example.foodcare.data.database.AppDatabase // Импорт для вашей БД
import com.example.foodcare.data.repository.ProductRepository // Импорт для репозитория
import com.example.foodcare.data.sync.FirebaseSyncManager // Импорт для синхронизации
import com.google.firebase.FirebaseApp

class FoodCareApplication : Application() {

    // --- ДОБАВЛЕНО: Создаём экземпляры зависимостей ---
    // (Это будет "Singleton" для приложения)
    lateinit var database: AppDatabase
        private set
    lateinit var productDao: com.example.foodcare.data.dao.ProductDao // Убедитесь, что путь правильный
        private set
    lateinit var userManager: UserManager
        private set
    lateinit var syncManager: FirebaseSyncManager
        private set
    lateinit var productRepository: ProductRepository
        private set
    // --- КОНЕЦ ДОБАВЛЕНИЯ ---

    companion object {
        @get:Synchronized
        lateinit var context: android.content.Context
            private set
    }

    override fun onCreate() {
        super.onCreate()

        context = applicationContext

        // Инициализация Firebase
        FirebaseApp.initializeApp(this)

        // --- ДОБАВЛЕНО: Инициализация зависимостей вручную ---
        database = Room.databaseBuilder(
            context,
            AppDatabase::class.java, // Убедитесь, что AppDatabase существует
            "foodcare_database"
        ).build()

        productDao = database.productDao()

        userManager = UserManager(context) // Передаём Context

        syncManager = FirebaseSyncManager(
            productDao = productDao, // Передаём ProductDao
            userManager = userManager // Передаём UserManager
        )

        productRepository = ProductRepository(
            productDao = productDao, // Передаём ProductDao
            syncManager = syncManager // Передаём SyncManager
        )
        // --- КОНЕЦ ДОБАВЛЕНИЯ ---
    }
}