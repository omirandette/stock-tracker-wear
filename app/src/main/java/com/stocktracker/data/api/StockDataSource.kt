package com.stocktracker.data.api

import com.stocktracker.model.ChartPoint
import com.stocktracker.model.TimePeriod

interface StockDataSource {
    suspend fun getQuote(symbol: String): QuoteResult
    suspend fun getChartData(symbol: String, period: TimePeriod): List<ChartPoint>
}

data class QuoteResult(
    val symbol: String,
    val price: Double,
    val change: Double,
    val changePercent: String,
)
