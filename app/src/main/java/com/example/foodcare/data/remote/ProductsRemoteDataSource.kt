package com.example.foodcare.data.remote

import com.example.foodcare.network.OFFProduct
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ProductsRemoteDataSource {

    private val apiService: ProductsApiService by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        Retrofit.Builder()
            .baseUrl("https://world.openfoodfacts.org")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ProductsApiService::class.java)
    }

    suspend fun searchProducts(query: String): List<OFFProduct> {
        return try {
            val response = apiService.searchProducts(query)
            response.products
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
