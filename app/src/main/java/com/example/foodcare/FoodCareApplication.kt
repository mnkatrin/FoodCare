package com.example.foodcare

import android.app.Application
import androidx.room.Room
import com.example.foodcare.auth.UserManager
import com.example.foodcare.data.database.AppDatabase
import com.example.foodcare.data.repository.ProductRepository
import com.example.foodcare.data.sync.FirebaseSyncManager
import com.example.foodcare.data.remote.ProductsRemoteDataSource // <-- Добавлен импорт
import com.google.firebase.FirebaseApp

class FoodCareApplication : Application() {


    lateinit var database: AppDatabase
        private set
    lateinit var productDao: com.example.foodcare.data.dao.ProductDao
        private set
    lateinit var userManager: UserManager
        private set
    lateinit var syncManager: FirebaseSyncManager
        private set
    lateinit var remoteDataSource: ProductsRemoteDataSource
        private set
    lateinit var productRepository: ProductRepository
        private set


    companion object {
        @get:Synchronized
        lateinit var context: android.content.Context
            private set
    }

    override fun onCreate() {
        super.onCreate()

        context = applicationContext

        FirebaseApp.initializeApp(this)


        database = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "foodcare_database"
        ).build()

        productDao = database.productDao()

        userManager = UserManager(context)

        syncManager = FirebaseSyncManager(
            productDao = productDao,
            userManager = userManager
        )


        remoteDataSource = ProductsRemoteDataSource()


        productRepository = ProductRepository(
            productDao = productDao,
            syncManager = syncManager,
            remoteDataSource = remoteDataSource
        )
    }
}