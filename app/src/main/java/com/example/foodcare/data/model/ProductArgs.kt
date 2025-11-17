package com.example.foodcare.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ProductArgs(
    val name: String = "",
    val category: String = "",
    val imageUrl: String = "",
    val quantity: Double = 1.0,
    val unit: String = "шт",
    val expirationDate: String = ""
) : Parcelable
