package com.example.foodcare

import android.app.Application
import com.example.foodcare.data.database.AppDatabase
import com.example.foodcare.data.repository.ProductRepository
import com.example.foodcare.data.sync.FirebaseSyncManager
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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

        // Инициализация категорий при запуске приложения
        initializeCategories()
    }

    private fun initializeCategories() {
        // Инициализируем категории в фоновом потоке
        CoroutineScope(Dispatchers.IO).launch {
            productRepository.initializeCategories()
        }
    }
}