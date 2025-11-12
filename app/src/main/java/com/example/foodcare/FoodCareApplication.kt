package com.example.foodcare

import android.app.Application
import com.example.foodcare.auth.UserManager
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp // <-- Добавлен импорт

// <-- Добавлена аннотация
@HiltAndroidApp
class FoodCareApplication : Application() {

    // УБРАТЬ: lateinit var userManager: UserManager
    // УБРАТЬ: lateinit var productRepository: ProductRepository
    // УБРАТЬ: инициализацию зависимостей из onCreate

    // Оставить только:
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

        // УБРАТЬ: инициализацию UserManager и productRepository
        // Hilt будет управлять зависимостями
    }
}