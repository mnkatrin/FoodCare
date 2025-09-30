package com.example.data.database.entities

import androidx.room.*
import java.util.UUID

@Entity(
    tableName = "user_products",
    indices = [Index("user_id"), Index("expiry_date")]
)
data class UserProduct(
    @PrimaryKey
    val product_id: String = UUID.randomUUID().toString(),

    val user_id: String,

    val barcode: String?,

    @ColumnInfo(name = "custom_name")
    val customName: String?,

    @ColumnInfo(name = "expiry_date")
    val expiryDate: String,

    val quantity: Int = 1,

    @ColumnInfo(name = "image_uri")
    val imageUri: String?,

    @ColumnInfo(name = "date_added")
    val dateAdded: Long = System.currentTimeMillis(),

    val status: String = "active",

    @ColumnInfo(name = "last_modified")
    val lastModified: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "is_synced")
    val isSynced: Boolean = false,

    @ColumnInfo(name = "deleted")
    val deleted: Boolean = false
)