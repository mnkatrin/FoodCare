package com.example.data.database.dao

import androidx.room.*
import com.example.data.database.entities.UserProduct
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: UserProduct)

    @Update
    suspend fun updateProduct(product: UserProduct)

    @Query("UPDATE user_products SET status = :status WHERE product_id = :productId")
    suspend fun updateProductStatus(productId: String, status: String)

    @Query("SELECT * FROM user_products WHERE user_id = :userId AND status = 'active'")
    fun getActiveProducts(userId: String): Flow<List<UserProduct>>

    @Query("SELECT * FROM user_products WHERE user_id = :userId AND status = 'active' ORDER BY expiry_date ASC")
    fun getActiveProductsSortedByExpiry(userId: String): Flow<List<UserProduct>>

    @Query("SELECT * FROM user_products WHERE is_synced = 0 AND deleted = 0")
    suspend fun getUnsyncedProducts(): List<UserProduct>

    @Query("UPDATE user_products SET is_synced = 1 WHERE product_id IN (:productIds)")
    suspend fun markAsSynced(productIds: List<String>)

    @Query("SELECT * FROM user_products WHERE product_id = :productId")
    suspend fun getProductById(productId: String): UserProduct?

    @Query("DELETE FROM user_products WHERE product_id = :productId")
    suspend fun deleteProduct(productId: String)
}