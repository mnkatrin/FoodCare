package com.example.foodcare.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "history_events")
data class HistoryEvent(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    val userId: String = "",          // Id пользователя (если используешь)
    val productId: String? = null,    // id продукта из таблицы products

    val productName: String,          // название продукта
    val category: String,             // категория продукта

    /**
     * Тип действия:
     *  - "USED"      – использовано
     *  - "DISCARDED" – выброшено / удалено как просроченное
     */
    val actionType: String,

    /**
     * Человекочитаемый текст для списка, например:
     *  "Использовано 0.5 л"
     *  "Выброшено 2 шт."
     */
    val quantityText: String,

    /**
     * Время события в миллисекундах (System.currentTimeMillis()).
     * В истории мы будем отображать только дату.
     */
    val createdAt: Long
)
