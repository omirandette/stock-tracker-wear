package com.stocktracker.data.api

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Query

interface StockApiService {
    @GET("query")
    suspend fun getQuote(
        @Query("function") function: String = "GLOBAL_QUOTE",
        @Query("symbol") symbol: String,
        @Query("apikey") apiKey: String,
    ): StockResponse

    @GET("query")
    suspend fun getTimeSeries(
        @Query("function") function: String,
        @Query("symbol") symbol: String,
        @Query("apikey") apiKey: String,
        @Query("interval") interval: String? = null,
        @Query("outputsize") outputSize: String? = null,
    ): ResponseBody
}
