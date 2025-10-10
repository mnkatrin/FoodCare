package com.example.foodcare.data.repository

import com.example.foodcare.data.dao.ProductDao
import com.example.foodcare.data.model.Product
import kotlinx.coroutines.flow.Flow

class ProductRepository(private val productDao: ProductDao) {

    fun getAllProducts(): Flow<List<Product>> = productDao.getAllProducts()

    // ДОБАВИТЬ suspend
    suspend fun addProduct(product: Product) = productDao.insertProduct(product)

    // ДОБАВИТЬ suspend
    suspend fun deleteProduct(product: Product) = productDao.deleteProduct(product)

    // ДОБАВИТЬ suspend
    suspend fun addSampleProducts() {
        val currentTime = System.currentTimeMillis()
        val dayInMillis = 24 * 60 * 60 * 1000L

        val sampleProducts = listOf(
            Product(name = "Молоко", category = "Молочные продукты", expirationDate = currentTime + 1 * dayInMillis, quantity = "1 л"),
            Product(name = "Хлеб", category = "Хлебобулочные", expirationDate = currentTime + 2 * dayInMillis, quantity = "1 буханка"),
            Product(name = "Йогурт", category = "Молочные продукты", expirationDate = currentTime + 3 * dayInMillis, quantity = "4 шт"),
            Product(name = "Сыр", category = "Молочные продукты", expirationDate = currentTime + 5 * dayInMillis, quantity = "200 г"),
            Product(name = "Курица", category = "Мясо", expirationDate = currentTime + 4 * dayInMillis, quantity = "1 кг"),
            Product(name = "Рыба", category = "Рыба", expirationDate = currentTime + 2 * dayInMillis, quantity = "500 г"),
            Product(name = "Яйца", category = "Яйца", expirationDate = currentTime + 14 * dayInMillis, quantity = "10 шт"),
            Product(name = "Яблоки", category = "Фрукты", expirationDate = currentTime + 10 * dayInMillis, quantity = "5 шт"),
            Product(name = "Морковь", category = "Овощи", expirationDate = currentTime + 8 * dayInMillis, quantity = "3 шт"),
            Product(name = "Консервы", category = "Консервы", expirationDate = currentTime + 365 * dayInMillis, quantity = "2 банки")
        )

        // Теперь это suspend вызов
        sampleProducts.forEach { product ->
            productDao.insertProduct(product)
        }
    }
}