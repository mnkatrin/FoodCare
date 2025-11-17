package com.example.foodcare.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.text.SimpleDateFormat
import java.util.*

@Parcelize
@Entity(tableName = "products")
data class Product(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val category: String = "",
    val expirationDate: String = "",
    var quantity: Double = 0.0,
    val unit: String = "", // кг, шт, л
    val barcode: String = "",
    val imageUrl: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val isDirty: Boolean = true,
    val firebaseId: String? = null,
    val lastSynced: Long? = null,
    val isDeleted: Boolean = false,
    val isMyProduct: Boolean = true,
    val userId: String = ""
) : Parcelable {

    fun getDaysUntilExpiration(): Int {
        return try {
            val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            val expiration = dateFormat.parse(expirationDate)
            val today = Calendar.getInstance().time
            if (expiration != null) {
                ((expiration.time - today.time) / (24 * 60 * 60 * 1000)).toInt()
            } else -1
        } catch (e: Exception) {
            -1
        }
    }

    fun isExpired(): Boolean = getDaysUntilExpiration() < 0

    fun getExpirationColor(): Int {
        val daysLeft = getDaysUntilExpiration()
        return when {
            daysLeft < 0 -> android.R.color.holo_red_light
            daysLeft <= 3 -> android.R.color.holo_orange_light
            else -> android.R.color.holo_green_light
        }
    }
}
