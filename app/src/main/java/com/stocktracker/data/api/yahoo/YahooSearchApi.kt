package com.stocktracker.data.api.yahoo

import retrofit2.http.GET
import retrofit2.http.Query

interface YahooSearchApi {
    @GET("v1/finance/search")
    suspend fun search(
        @Query("q") query: String,
        @Query("quotesCount") quotesCount: Int = 10,
        @Query("newsCount") newsCount: Int = 0,
    ): YahooSearchResponse
}
