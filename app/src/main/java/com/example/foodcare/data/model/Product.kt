package com.example.foodcare.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.*

@Entity(tableName = "products")
data class Product(
    @PrimaryKey
    val id: String = "",
    val name: String = "",
    val category: String = "",
    val expirationDate: String = "", // в формате "dd.MM.yyyy"
    val quantity: Double = 0.0,
    val unit: String = "", // гр, мл, кг, шт
    val barcode: String = "",
    val imageUrl: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val isDirty: Boolean = true,
    val firebaseId: String? = null,
    val lastSynced: Long? = null,
    val isDeleted: Boolean = false
) {

    // Метод для получения дней до истечения срока годности
    fun getDaysUntilExpiration(): Int {
        return try {
            val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            val expiration = dateFormat.parse(expirationDate)
            val today = Calendar.getInstance().time

            if (expiration != null) {
                val diff = expiration.time - today.time
                val days = diff / (24 * 60 * 60 * 1000)
                days.toInt()
            } else {
                -1 // если дата невалидная
            }
        } catch (e: Exception) {
            -1 // в случае ошибки парсинга
        }
    }

    // Дополнительный метод для проверки просрочен ли продукт
    fun isExpired(): Boolean {
        return getDaysUntilExpiration() < 0
    }

    // Метод для получения цвета в зависимости от срока годности
    fun getExpirationColor(): Int {
        val daysLeft = getDaysUntilExpiration()
        return when {
            daysLeft < 0 -> android.R.color.holo_red_light // Просрочен
            daysLeft <= 3 -> android.R.color.holo_orange_light // Скоро истечет
            else -> android.R.color.holo_green_light // Нормальный срок
        }
    }
}