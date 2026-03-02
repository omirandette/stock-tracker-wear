package com.stocktracker.testutil

import com.stocktracker.data.api.QuoteResult
import com.stocktracker.data.api.yahoo.ChartBody
import com.stocktracker.data.api.yahoo.ChartError
import com.stocktracker.data.api.yahoo.ChartMeta
import com.stocktracker.data.api.yahoo.ChartResult
import com.stocktracker.data.api.yahoo.Indicators
import com.stocktracker.data.api.yahoo.QuoteIndicator
import com.stocktracker.data.api.yahoo.YahooChartResponse
import com.stocktracker.data.api.yahoo.YahooSearchQuote
import com.stocktracker.data.api.yahoo.YahooSearchResponse

fun chartResponse(
    symbol: String = "AAPL",
    price: Double = 150.0,
    previousClose: Double = 148.0,
    chartPreviousClose: Double? = null,
    timestamps: List<Long>? = listOf(1000L, 2000L),
    closes: List<Double?>? = listOf(149.0, 150.0),
    error: ChartError? = null,
): YahooChartResponse = YahooChartResponse(
    chart = ChartBody(
        result = if (error != null) null else listOf(
            ChartResult(
                meta = ChartMeta(symbol, price, previousClose, chartPreviousClose),
                timestamp = timestamps,
                indicators = Indicators(listOf(QuoteIndicator(closes))),
            )
        ),
        error = error,
    )
)

fun quoteResult(
    symbol: String = "AAPL",
    price: Double = 150.0,
    change: Double = 2.0,
    changePercent: String = "1.35%",
    lastUpdated: Long = System.currentTimeMillis(),
) = QuoteResult(symbol, price, change, changePercent, lastUpdated)

fun searchResponse(
    quotes: List<YahooSearchQuote> = listOf(
        YahooSearchQuote("AAPL", "Apple Inc.", "Apple Inc.", "NASDAQ", "EQUITY"),
        YahooSearchQuote("APLE", "Apple Hospitality REIT", null, "NYSE", "EQUITY"),
    ),
) = YahooSearchResponse(quotes = quotes)
