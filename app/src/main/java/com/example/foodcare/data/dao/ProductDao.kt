package com.example.foodcare.data.dao

import androidx.room.*
import com.example.foodcare.data.model.Category
import com.example.foodcare.data.model.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    // МЕТОДЫ ДЛЯ PRODUCTS
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

    // МЕТОДЫ ДЛЯ CATEGORIES
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category)

    @Query("SELECT * FROM categories ORDER BY name")
    fun getAllCategories(): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE id = :categoryId")
    suspend fun getCategoryById(categoryId: String): Category?

    @Query("DELETE FROM categories")
    suspend fun deleteAllCategories()
}