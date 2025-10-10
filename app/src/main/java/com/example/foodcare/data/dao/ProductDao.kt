package com.example.foodcare.data.dao

import androidx.room.*
import com.example.foodcare.data.model.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY expirationDate ASC")
    fun getAllProducts(): Flow<List<Product>>

    // УБРАТЬ suspend
    @Insert
    fun insertProduct(product: Product)

    // УБРАТЬ suspend
    @Delete
    fun deleteProduct(product: Product)
}