package com.example.foodcare.data.repository

import com.example.foodcare.data.dao.ProductDao
import com.example.foodcare.data.model.Category
import com.example.foodcare.data.model.Product
import com.example.foodcare.data.sync.FirebaseSyncManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ProductRepository @Inject constructor(
    private val productDao: ProductDao,
    private val syncManager: FirebaseSyncManager
) {

    // МЕТОДЫ ДЛЯ ПРОДУКТОВ
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

    // ДОБАВЬТЕ ЭТОТ МЕТОД ДЛЯ ТЕСТОВЫХ ДАННЫХ
    suspend fun addSampleProducts() {
        // Проверим, есть ли уже продукты
        val existingProducts = productDao.getAllProducts().first()
        if (existingProducts.isNotEmpty()) {
            return // Продукты уже есть, не добавляем снова
        }

        val sampleProducts = listOf(
            Product(
                name = "Молоко",
                category = "Молочные продукты",
                expirationDate = "25.12.2024",
                quantity = 1.0,
                unit = "л"
            ),
            Product(
                name = "Хлеб",
                category = "Хлебобулочные изделия",
                expirationDate = "20.12.2024",
                quantity = 1.0,
                unit = "шт"
            ),
            Product(
                name = "Яйца",
                category = "Яйца",
                expirationDate = "30.12.2024",
                quantity = 10.0,
                unit = "шт"
            ),
            Product(
                name = "Яблоки",
                category = "Фрукты",
                expirationDate = "28.12.2024",
                quantity = 1.5,
                unit = "кг"
            ),
            Product(
                name = "Апельсиновый сок",
                category = "Напитки",
                expirationDate = "15.01.2025",
                quantity = 1.0,
                unit = "л"
            ),
            Product(
                name = "Куриное филе",
                category = "Мясо, птица",
                expirationDate = "22.12.2024",
                quantity = 0.5,
                unit = "кг"
            )
        )

        sampleProducts.forEach { product ->
            productDao.insertProduct(product.copy(isDirty = true))
        }

        // Синхронизируем тестовые данные
        syncManager.syncIfNeeded()
    }

    // МЕТОДЫ ДЛЯ КАТЕГОРИЙ
    fun getAllCategories(): Flow<List<Category>> = productDao.getAllCategories()

    suspend fun initializeCategories() {
        // Проверим, есть ли уже категории
        val existingCategories = productDao.getAllCategories().first()
        if (existingCategories.isNotEmpty()) {
            return // Категории уже есть, не добавляем снова
        }

        val defaultCategories = listOf(
            Category("1", "Молочные продукты"),
            Category("2", "Мясо, птица"),
            Category("3", "Овощи"),
            Category("4", "Фрукты"),
            Category("5", "Напитки"),
            Category("6", "Хлебобулочные изделия"),
            Category("7", "Бакалея"),
            Category("8", "Замороженные продукты"),
            Category("9", "Сладости"),
            Category("10", "Яйца"),
            Category("11", "Консервы"),
            Category("12", "Прочее")
        )

        defaultCategories.forEach { category ->
            productDao.insertCategory(category)
        }
    }

    // ДОПОЛНИТЕЛЬНЫЕ МЕТОДЫ ДЛЯ УДОБСТВА
    suspend fun getProductsByCategory(category: String): List<Product> {
        val allProducts = productDao.getAllProducts().first()
        return allProducts.filter { it.category == category && !it.isDeleted }
    }

    suspend fun getExpiringSoonProducts(days: Int = 3): List<Product> {
        val threshold = System.currentTimeMillis() + (days * 24 * 60 * 60 * 1000L)
        val currentTime = System.currentTimeMillis()

        val allProducts = productDao.getAllProducts().first()
        return allProducts.filter { product ->
            !product.isDeleted && product.expirationDate.toLongOrNull() in (currentTime + 1)..threshold
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