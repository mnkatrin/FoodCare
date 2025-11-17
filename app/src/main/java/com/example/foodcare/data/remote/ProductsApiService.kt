package com.example.foodcare.data.remote

import com.example.foodcare.network.OpenFoodFactsResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ProductsApiService {

    @GET("/cgi/search.pl")
    suspend fun searchProducts(
        @Query("search_terms") query: String,
        @Query("search_simple") searchSimple: Int = 1,
        @Query("action") action: String = "process",
        @Query("json") json: Int = 1,
        @Query("page_size") pageSize: Int = 20,
        @Query("lc") language: String = "ru" // язык — русский
    ): OpenFoodFactsResponse
}
