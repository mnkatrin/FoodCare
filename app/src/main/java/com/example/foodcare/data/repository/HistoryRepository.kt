package com.example.foodcare.data.repository

import com.example.foodcare.data.dao.HistoryDao
import com.example.foodcare.data.model.HistoryEvent
import com.example.foodcare.data.model.Product
import kotlinx.coroutines.flow.Flow

class HistoryRepository(
    private val historyDao: HistoryDao
) {

    fun getHistory(userId: String? = null): Flow<List<HistoryEvent>> {
        return if (userId.isNullOrEmpty()) {
            historyDao.getAllHistory()
        } else {
            historyDao.getHistoryByUser(userId)
        }
    }

    suspend fun addUsedFromProduct(product: Product) {
        val quantityText = buildQuantityText(
            prefix = "Использовано",
            quantity = product.quantity,
            unit = product.unit
        )

        val event = HistoryEvent(
            userId = product.userId,
            productId = product.id,
            productName = product.name,
            category = product.category,
            actionType = "USED",
            quantityText = quantityText,
            createdAt = System.currentTimeMillis()
        )

        historyDao.insertEvent(event)
    }

    suspend fun addDiscardedFromProduct(product: Product) {
        val quantityText = buildQuantityText(
            prefix = "Выброшено",
            quantity = product.quantity,
            unit = product.unit
        )

        val event = HistoryEvent(
            userId = product.userId,
            productId = product.id,
            productName = product.name,
            category = product.category,
            actionType = "DISCARDED",
            quantityText = quantityText,
            createdAt = System.currentTimeMillis()
        )

        historyDao.insertEvent(event)
    }

    private fun buildQuantityText(prefix: String, quantity: Double, unit: String): String {
        val value = if (quantity % 1.0 == 0.0) {
            quantity.toInt().toString()
        } else {
            String.format("%.1f", quantity)
        }

        return if (unit.isBlank()) {
            "$prefix $value"
        } else {
            "$prefix $value $unit"
        }
    }
}
