package com.example.foodcare.data.repository

import com.example.foodcare.data.dao.ProductDao
import com.example.foodcare.data.model.Product
import com.example.foodcare.data.sync.FirebaseSyncManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ProductRepository @Inject constructor(
    private val productDao: ProductDao,
    private val syncManager: FirebaseSyncManager
) {

    fun getAllProducts(): Flow<List<Product>> = productDao.getAllProducts()

    suspend fun addProduct(product: Product) {
        // Сохраняем локально с пометкой для синхронизации
        val productToSave = product.copy(isDirty = true)
        productDao.insertProduct(productToSave)

        // Запускаем синхронизацию
        syncManager.syncIfNeeded()
    }

    suspend fun deleteProduct(product: Product) {
        if (product.firebaseId != null) {
            // Помечаем для удаления и синхронизации
            val deletedProduct = product.copy(isDeleted = true, isDirty = true)
            productDao.updateProduct(deletedProduct)
        } else {
            // Просто удаляем локально (еще не синхронизирован)
            productDao.deleteProduct(product)
        }
        syncManager.syncIfNeeded()
    }

    suspend fun updateProduct(product: Product) {
        // Помечаем как измененное для синхронизации
        val updatedProduct = product.copy(isDirty = true)
        productDao.updateProduct(updatedProduct)
        syncManager.syncIfNeeded()
    }

    suspend fun addSampleProducts() {
        // Проверим, есть ли уже продукты
        val existingProducts = productDao.getAllProducts().first()
        if (existingProducts.isNotEmpty()) {
            return // Продукты уже есть, не добавляем снова
        }

        val currentTime = System.currentTimeMillis()
        val dayInMillis = 24 * 60 * 60 * 1000L

        val sampleProducts = listOf(
            // Молочные продукты - литры/миллилитры
            Product(name = "Молоко", category = "Молочные продукты", expirationDate = currentTime + 2 * dayInMillis, quantity = "1 л"),
            Product(name = "Йогурт", category = "Молочные продукты", expirationDate = currentTime + 3 * dayInMillis, quantity = "4 шт"),
            Product(name = "Сыр", category = "Молочные продукты", expirationDate = currentTime + 5 * dayInMillis, quantity = "200 г"),
            Product(name = "Творог", category = "Молочные продукты", expirationDate = currentTime + 4 * dayInMillis, quantity = "400 г"),
            Product(name = "Сметана", category = "Молочные продукты", expirationDate = currentTime + 3 * dayInMillis, quantity = "300 г"),
            Product(name = "Кефир", category = "Молочные продукты", expirationDate = currentTime + 2 * dayInMillis, quantity = "1 л"),
            Product(name = "Сливки", category = "Молочные продукты", expirationDate = currentTime + 3 * dayInMillis, quantity = "500 мл"),

            // Хлебобулочные - штуки
            Product(name = "Хлеб", category = "Хлебобулочные", expirationDate = currentTime + 2 * dayInMillis, quantity = "1 шт"),
            Product(name = "Булочки", category = "Хлебобулочные", expirationDate = currentTime + 3 * dayInMillis, quantity = "6 шт"),
            Product(name = "Печенье", category = "Хлебобулочные", expirationDate = currentTime + 15 * dayInMillis, quantity = "1 шт"),
            Product(name = "Круассан", category = "Хлебобулочные", expirationDate = currentTime + 2 * dayInMillis, quantity = "2 шт"),

            // Яйца - штуки
            Product(name = "Яйца", category = "Яйца", expirationDate = currentTime + 14 * dayInMillis, quantity = "10 шт"),

            // Фрукты - штуки/килограммы
            Product(name = "Яблоки", category = "Фрукты", expirationDate = currentTime + 10 * dayInMillis, quantity = "1 кг"),
            Product(name = "Бананы", category = "Фрукты", expirationDate = currentTime + 5 * dayInMillis, quantity = "3 шт"),
            Product(name = "Апельсины", category = "Фрукты", expirationDate = currentTime + 12 * dayInMillis, quantity = "4 шт"),
            Product(name = "Груши", category = "Фрукты", expirationDate = currentTime + 8 * dayInMillis, quantity = "2 шт"),
            Product(name = "Виноград", category = "Фрукты", expirationDate = currentTime + 7 * dayInMillis, quantity = "500 г"),

            // Овощи - штуки/килограммы
            Product(name = "Морковь", category = "Овощи", expirationDate = currentTime + 8 * dayInMillis, quantity = "3 шт"),
            Product(name = "Картофель", category = "Овощи", expirationDate = currentTime + 20 * dayInMillis, quantity = "2 кг"),
            Product(name = "Лук", category = "Овощи", expirationDate = currentTime + 15 * dayInMillis, quantity = "5 шт"),
            Product(name = "Помидоры", category = "Овощи", expirationDate = currentTime + 7 * dayInMillis, quantity = "4 шт"),
            Product(name = "Огурцы", category = "Овощи", expirationDate = currentTime + 6 * dayInMillis, quantity = "3 шт"),
            Product(name = "Капуста", category = "Овощи", expirationDate = currentTime + 12 * dayInMillis, quantity = "1 шт"),

            // Консервы - штуки
            Product(name = "Консервы", category = "Консервы", expirationDate = currentTime + 365 * dayInMillis, quantity = "2 шт"),

            // Бакалея - граммы/килограммы
            Product(name = "Макароны", category = "Бакалея", expirationDate = currentTime + 180 * dayInMillis, quantity = "400 г"),
            Product(name = "Рис", category = "Бакалея", expirationDate = currentTime + 200 * dayInMillis, quantity = "1 кг"),
            Product(name = "Гречка", category = "Бакалея", expirationDate = currentTime + 190 * dayInMillis, quantity = "800 г"),
            Product(name = "Овсянка", category = "Бакалея", expirationDate = currentTime + 150 * dayInMillis, quantity = "500 г"),
            Product(name = "Мука", category = "Бакалея", expirationDate = currentTime + 120 * dayInMillis, quantity = "1 кг"),
            Product(name = "Сахар", category = "Бакалея", expirationDate = currentTime + 300 * dayInMillis, quantity = "1 кг"),

            // Напитки - литры/миллилитры
            Product(name = "Сок", category = "Напитки", expirationDate = currentTime + 30 * dayInMillis, quantity = "1 л"),
            Product(name = "Вода", category = "Напитки", expirationDate = currentTime + 90 * dayInMillis, quantity = "1.5 л"),
            Product(name = "Чай", category = "Напитки", expirationDate = currentTime + 180 * dayInMillis, quantity = "100 г"),
            Product(name = "Кофе", category = "Напитки", expirationDate = currentTime + 200 * dayInMillis, quantity = "250 г")
        )

        // Добавляем продукты без пометки синхронизации (это тестовые данные)
        sampleProducts.forEach { product ->
            productDao.insertProduct(product)
        }

        // Синхронизируем тестовые данные с Firebase
        syncManager.syncIfNeeded()
    }

    // Дополнительные методы для удобства
    suspend fun getProductsByCategory(category: String): List<Product> {
        val allProducts = productDao.getAllProducts().first()
        return allProducts.filter { it.category == category && !it.isDeleted }
    }

    suspend fun getExpiringSoonProducts(days: Int = 3): List<Product> {
        val threshold = System.currentTimeMillis() + (days * 24 * 60 * 60 * 1000L)
        val currentTime = System.currentTimeMillis()

        val allProducts = productDao.getAllProducts().first()
        return allProducts.filter { product ->
            !product.isDeleted && product.expirationDate in (currentTime + 1)..threshold
        }
    }

    suspend fun forceSync() {
        syncManager.syncAllData()
    }

    // Дополнительный метод для очистки базы (если нужно)
    suspend fun deleteAllProducts() {
        productDao.deleteAllProducts()
    }
}