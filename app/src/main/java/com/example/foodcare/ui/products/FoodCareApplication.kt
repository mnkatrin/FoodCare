package com.example.foodcare

import android.app.Application
import com.example.foodcare.data.database.AppDatabase
import com.example.foodcare.data.repository.ProductRepository
import com.example.foodcare.data.sync.FirebaseSyncManager
import com.google.firebase.FirebaseApp

class FoodCareApplication : Application() {

    // Используем lazy инициализацию как в вашем коде
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }

    val productRepository: ProductRepository by lazy {
        ProductRepository(
            productDao = database.productDao(),
            syncManager = FirebaseSyncManager(database.productDao())
        )
    }

    override fun onCreate() {
        super.onCreate()

        // Инициализация Firebase
        FirebaseApp.initializeApp(this)

        // База данных инициализируется при первом обращении через lazy
        // Можно добавить предзагрузку тестовых данных если нужно
        preloadSampleData()
    }

    private fun preloadSampleData() {
        // Опционально: предзагрузка тестовых данных при первом запуске
        // Это выполнится в фоновом потоке при первом обращении к repository
    }
}