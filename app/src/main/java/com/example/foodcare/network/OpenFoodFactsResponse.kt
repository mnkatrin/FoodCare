package com.example.foodcare.network

import com.google.gson.annotations.SerializedName

data class OpenFoodFactsResponse(
    @SerializedName("products")
    val products: List<OFFProduct> = emptyList()
)
