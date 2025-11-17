package com.example.foodcare.network

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class OFFProduct(
    @SerializedName("product_name") val productName: String? = null,
    @SerializedName("product_name_ru") val productNameRu: String? = null,
    @SerializedName("categories") val categories: String? = null,
    @SerializedName("image_url") val imageUrl: String? = null
) : Parcelable
