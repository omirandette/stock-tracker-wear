package com.stocktracker.data.api.yahoo

data class YahooChartResponse(
    val chart: ChartBody,
)

data class ChartBody(
    val result: List<ChartResult>?,
    val error: ChartError?,
)

data class ChartResult(
    val meta: ChartMeta,
    val timestamp: List<Long>?,
    val indicators: Indicators?,
)

data class ChartMeta(
    val symbol: String,
    val regularMarketPrice: Double,
    val previousClose: Double,
)

data class Indicators(
    val quote: List<QuoteIndicator>?,
)

data class QuoteIndicator(
    val close: List<Double?>?,
)

data class ChartError(
    val code: String?,
    val description: String?,
)
