package com.example.foodcare

import android.app.Application
import com.example.foodcare.data.database.AppDatabase
import com.google.firebase.FirebaseApp

class FoodCareApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Инициализация Firebase
        FirebaseApp.initializeApp(this)

        // Инициализация Room (ленивая)
        AppDatabase.getInstance(this)
    }
}