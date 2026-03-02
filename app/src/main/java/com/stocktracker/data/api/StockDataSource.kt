package com.stocktracker.data.api

import com.stocktracker.model.ChartData
import com.stocktracker.model.SearchResult
import com.stocktracker.model.TimePeriod

interface StockDataSource {
    suspend fun getQuote(symbol: String): QuoteResult
    suspend fun getChartData(symbol: String, period: TimePeriod): ChartData
    suspend fun searchStocks(query: String): List<SearchResult>
}

data class QuoteResult(
    val symbol: String,
    val price: Double,
    val change: Double,
    val changePercent: String,
)
