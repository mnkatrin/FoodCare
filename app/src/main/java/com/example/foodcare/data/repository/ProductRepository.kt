package com.example.foodcare.data.repository

import com.example.foodcare.data.dao.ProductDao
import com.example.foodcare.data.model.Category
import com.example.foodcare.data.model.Product
import com.example.foodcare.data.sync.FirebaseSyncManager
import com.example.foodcare.data.remote.ProductsRemoteDataSource // <-- Импорт RemoteDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf // <-- Импорт для возврата результата поиска

// Убираем аннотации Hilt
class ProductRepository(
    val productDao: ProductDao,
    val syncManager: FirebaseSyncManager,
    // --- ДОБАВЛЕНО: ProductsRemoteDataSource ---
    private val remoteDataSource: ProductsRemoteDataSource
    // --- КОНЕЦ ДОБАВЛЕНИЯ ---
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

    // --- УБРАНО: addSampleProducts ---
    // suspend fun addSampleProducts() { ... }
    // --- КОНЕЦ УБРАНО ---

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
            Category("12", "Прочее"),
            Category("13", "Снэки")
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

    // --- НОВЫЙ МЕТОД: Поиск продуктов через API ---
    // ProductRepository.kt
// ProductRepository.kt
    suspend fun searchProducts(query: String): List<Product> {
        if (query.isBlank()) return emptyList()

        val apiResults = remoteDataSource.searchProducts(query)

        return apiResults.map { apiProduct ->
            val name = apiProduct.productNameRu
                ?.takeIf { it.isNotBlank() }
                ?: apiProduct.productName.orEmpty()

            // Берём первую категорию, чистим и отбрасываем странные вроде "en:cheeses"
            val rawCategory = apiProduct.categories
                ?.split(',')
                ?.map { it.trim() }
                ?.firstOrNull()
                .orEmpty()

            val displayCategory = if (rawCategory.contains(":") || rawCategory.isBlank()) {
                ""
            } else {
                rawCategory
            }

            Product(
                name = name,
                category = displayCategory,
                expirationDate = "",
                quantity = 0.0,
                unit = "",
                barcode = "",
                imageUrl = apiProduct.imageUrl.orEmpty(),
                isMyProduct = false,
                userId = ""
            )
        }
    }



    private fun String?.orElseEmpty(): String = this ?: ""

    // МЕТОДЫ ДЛЯ БУДУЩЕЙ ИНТЕГРАЦИИ С ИИ (оставляем как есть, но они не используются для поиска)
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