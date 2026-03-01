package com.stocktracker.data.api.yahoo

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface YahooChartApi {
    @GET("v8/finance/chart/{symbol}")
    suspend fun getChart(
        @Path("symbol") symbol: String,
        @Query("range") range: String,
        @Query("interval") interval: String,
    ): YahooChartResponse
}
