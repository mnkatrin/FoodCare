package com.example.foodcare.history

data class HistoryFilter(
    val status: String? = null,         // "USED", "DISCARDED" или null (все)
    val query: String = "",             // поиск по названию
    val category: String? = null,       // фильтр по категории
    val dateFromMillis: Long? = null,   // дата "с"
    val dateToMillis: Long? = null      // дата "по"
)
