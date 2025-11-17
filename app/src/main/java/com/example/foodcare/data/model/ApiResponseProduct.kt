package com.example.foodcare.data.model

import com.google.gson.annotations.SerializedName

// Модель данных, которая приходит из API поиска продуктов
data class ApiResponseProduct(
    @SerializedName("product_name") val productName: String?,
    @SerializedName("categories") val category: String?,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("image_thumb_url") val imageThumbUrl: String?
)
