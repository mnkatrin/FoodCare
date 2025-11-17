package com.example.foodcare.network

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenFoodFactsApi {
    @GET("cgi/search.pl?search_simple=1&action=process&json=1&page_size=20")
    suspend fun searchProducts(
        @Query("search_terms") query: String
    ): Response<OpenFoodFactsResponse>
}
