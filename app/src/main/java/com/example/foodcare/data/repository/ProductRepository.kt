package com.example.foodcare.data.repository

import com.example.foodcare.data.dao.ProductDao
import com.example.foodcare.data.model.Category
import com.example.foodcare.data.model.Product
import com.example.foodcare.data.sync.FirebaseSyncManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

// --- УБРАНО: @Singleton, @Inject constructor ---
// import javax.inject.Inject
// import javax.inject.Singleton

// Убираем аннотации
class ProductRepository( // Убираем @Inject
    val productDao: ProductDao, // Убираем private, если нужно получить доступ извне
    val syncManager: FirebaseSyncManager // Убираем private, если нужно получить доступ извне
) {

    // СУЩЕСТВУЮЩИЕ МЕТОДЫ
    fun getAllProducts(): Flow<List<Product>> = productDao.getAllProducts()

    suspend fun addProduct(product: Product) {
        val productToSave = product.copy(isDirty = true)
        productDao.insertProduct(productToSave)
        syncManager.syncIfNeeded()
    }

    suspend fun deleteProduct(product: Product) {
        if (product.firebaseId != null) {
            val deletedProduct = product.copy(isDeleted = true, isDirty = true)
            productDao.updateProduct(deletedProduct)
        } else {
            productDao.deleteProduct(product)
        }
        syncManager.syncIfNeeded()
    }

    suspend fun updateProduct(product: Product) {
        val updatedProduct = product.copy(isDirty = true)
        productDao.updateProduct(updatedProduct)
        syncManager.syncIfNeeded()
    }

    // ОБНОВЛЕННЫЙ МЕТОД - добавляем userId к примерам продуктов
    suspend fun addSampleProducts() {
        val existingProducts = productDao.getAllProducts().first()
        if (existingProducts.isNotEmpty()) {
            return
        }

        // Для примеров продуктов используем специальный userId
        val sampleUserId = "sample_user"

        val sampleProducts = listOf(
            Product(
                name = "Молоко",
                category = "Молочные продукты",
                expirationDate = "25.12.2024",
                quantity = 1.0,
                unit = "л",
                isMyProduct = true,
                userId = sampleUserId
            ),
            Product(
                name = "Хлеб",
                category = "Хлебобулочные изделия",
                expirationDate = "20.12.2024",
                quantity = 1.0,
                unit = "шт",
                isMyProduct = true,
                userId = sampleUserId
            ),
            Product(
                name = "Яйца",
                category = "Яйца",
                expirationDate = "30.12.2024",
                quantity = 10.0,
                unit = "шт",
                isMyProduct = true,
                userId = sampleUserId
            ),
            Product(
                name = "Яблоки",
                category = "Фрукты",
                expirationDate = "28.12.2024",
                quantity = 1.5,
                unit = "кг",
                isMyProduct = true,
                userId = sampleUserId
            ),
            Product(
                name = "Апельсиновый сок",
                category = "Напитки",
                expirationDate = "15.01.2025",
                quantity = 1.0,
                unit = "л",
                isMyProduct = true,
                userId = sampleUserId
            ),
            Product(
                name = "Куриное филе",
                category = "Мясо, птица",
                expirationDate = "22.12.2024",
                quantity = 0.5,
                unit = "кг",
                isMyProduct = true,
                userId = sampleUserId
            )
        )

        sampleProducts.forEach { product ->
            productDao.insertProduct(product.copy(isDirty = true))
        }
        syncManager.syncIfNeeded()
    }

    fun getAllCategories(): Flow<List<Category>> = productDao.getAllCategories()

    suspend fun initializeCategories() {
        val existingCategories = productDao.getAllCategories().first()
        if (existingCategories.isNotEmpty()) {
            return
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

    suspend fun getProductsByCategory(category: String): List<Product> {
        val allProducts = productDao.getAllProducts().first()
        return allProducts.filter { it.category == category && !it.isDeleted }
    }

    suspend fun getExpiringSoonProducts(days: Int = 3): List<Product> {
        val allProducts = productDao.getAllProducts().first()
        return allProducts.filter { product ->
            !product.isDeleted && product.getDaysUntilExpiration() in 0..days
        }
    }

    suspend fun forceSync() {
        syncManager.syncAllData()
    }

    suspend fun deleteAllProducts() {
        productDao.deleteAllProducts()
    }

    // НОВЫЕ МЕТОДЫ ДЛЯ РАБОТЫ С ПОЛЬЗОВАТЕЛЯМИ
    suspend fun getMyProducts(userId: String): List<Product> {
        return productDao.getMyProductsByUser(userId)
    }

    suspend fun getRecentlyAddedProducts(userId: String, limit: Int = 10): List<Product> {
        return productDao.getRecentlyAddedByUser(userId, limit)
    }

    suspend fun getExpiringSoonByUser(userId: String, limit: Int = 5): List<Product> {
        return productDao.getExpiringSoonByUser(userId, limit)
    }

    suspend fun getProductsByCategoryAndUser(category: String, userId: String): List<Product> {
        return productDao.getProductsByCategoryAndUser(category, userId)
    }

    // МЕТОДЫ ДЛЯ БУДУЩЕЙ ИНТЕГРАЦИИ С ИИ
    suspend fun searchProductsWithAI(query: String, userId: String): List<Product> {
        // Пока используем обычный поиск, потом заменим на ИИ
        return productDao.searchProductsByUser(query, userId)
    }

    suspend fun getAIRecipeSuggestions(userId: String): List<String> {
        val userProducts = getMyProducts(userId)

        // Временная логика рекомендаций на основе категорий продуктов пользователя
        val categories = userProducts.map { it.category }.distinct()

        return when {
            categories.contains("Овощи") && categories.contains("Мясо, птица") ->
                listOf("Овощное рагу с мясом", "Суп с овощами и курицей")
            categories.contains("Молочные продукты") && categories.contains("Яйца") ->
                listOf("Омлет с сыром", "Молочный коктейль")
            categories.contains("Фрукты") ->
                listOf("Фруктовый салат", "Смузи")
            else -> listOf("Быстрый ужин из доступных продуктов")
        }
    }
}