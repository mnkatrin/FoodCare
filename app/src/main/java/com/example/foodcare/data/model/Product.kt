package com.example.foodcare.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: String,
    val expirationDate: Long,
    val quantity: String = "1 шт.",
    val imageUri: String? = null,
    // Поля для синхронизации
    val firebaseId: String? = null,
    val lastSynced: Long = 0,
    val isDirty: Boolean = false,
    val isDeleted: Boolean = false
) {
    fun getDaysUntilExpiration(): Long {
        val currentTime = System.currentTimeMillis()
        val diff = expirationDate - currentTime
        return (diff / (1000 * 60 * 60 * 24)).coerceAtLeast(0)
    }

    val isExpired: Boolean
        get() = System.currentTimeMillis() > expirationDate

    val needsSync: Boolean
        get() = isDirty || firebaseId == null
}