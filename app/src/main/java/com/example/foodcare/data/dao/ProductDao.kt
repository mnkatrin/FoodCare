package com.example.foodcare.data.dao

import androidx.room.*
import com.example.foodcare.data.model.Category
import com.example.foodcare.data.model.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    // СУЩЕСТВУЮЩИЕ МЕТОДЫ ДЛЯ PRODUCTS
    @Query("SELECT * FROM products")
    fun getAllProducts(): Flow<List<Product>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product)

    @Delete
    suspend fun deleteProduct(product: Product)

    @Update
    suspend fun updateProduct(product: Product)

    @Query("DELETE FROM products")
    suspend fun deleteAllProducts()

    @Query("SELECT * FROM products WHERE isDirty = 1 OR firebaseId IS NULL")
    suspend fun getUnsyncedProducts(): List<Product>

    @Query("UPDATE products SET isDirty = 0, lastSynced = :timestamp WHERE id = :productId")
    suspend fun markAsSynced(productId: String, timestamp: Long)

    @Query("UPDATE products SET firebaseId = :firebaseId WHERE id = :localId")
    suspend fun updateFirebaseId(localId: String, firebaseId: String)

    @Query("SELECT * FROM products WHERE firebaseId = :firebaseId")
    suspend fun getProductByFirebaseId(firebaseId: String): Product?

    @Query("SELECT * FROM products WHERE isDeleted = 1")
    suspend fun getDeletedProducts(): List<Product>

    // НОВЫЕ МЕТОДЫ ДЛЯ РАБОТЫ С ПОЛЬЗОВАТЕЛЯМИ
    @Query("SELECT * FROM products WHERE userId = :userId AND isMyProduct = 1 AND isDeleted = 0 ORDER BY createdAt DESC")
    suspend fun getMyProductsByUser(userId: String): List<Product>

    @Query("SELECT * FROM products WHERE userId = :userId AND isMyProduct = 1 AND isDeleted = 0 ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getRecentlyAddedByUser(userId: String, limit: Int): List<Product>

    @Query("SELECT * FROM products WHERE userId = :userId AND (name LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%') AND isDeleted = 0")
    suspend fun searchProductsByUser(query: String, userId: String): List<Product>

    @Query("SELECT * FROM products WHERE userId = :userId AND category = :category AND isDeleted = 0 ORDER BY createdAt DESC")
    suspend fun getProductsByCategoryAndUser(category: String, userId: String): List<Product>

    @Query("SELECT * FROM products WHERE userId = :userId AND isDeleted = 0 AND expirationDate != '' ORDER BY expirationDate ASC LIMIT :limit")
    suspend fun getExpiringSoonByUser(userId: String, limit: Int = 5): List<Product>

    // МЕТОДЫ ДЛЯ CATEGORIES (оставляем без изменений)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category)

    @Query("SELECT * FROM categories ORDER BY name")
    fun getAllCategories(): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE id = :categoryId")
    suspend fun getCategoryById(categoryId: String): Category?

    @Query("DELETE FROM categories")
    suspend fun deleteAllCategories()
}