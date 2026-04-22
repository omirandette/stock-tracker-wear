package com.stocktracker.phone.testutil

import com.stocktracker.shared.data.api.QuoteResult
import com.stocktracker.shared.data.api.StockDataSource
import com.stocktracker.shared.model.ChartData
import com.stocktracker.shared.model.SearchResult
import com.stocktracker.shared.model.TimePeriod

class ConfigurableFakeDataSource(
    var quoteHandler: suspend (String) -> QuoteResult = { symbol ->
        QuoteResult(symbol, 100.0, 1.0, "1.00%", System.currentTimeMillis())
    },
    var chartHandler: suspend (String, TimePeriod) -> ChartData = { _, _ ->
        ChartData(emptyList(), 0.0, 0.0)
    },
    var searchHandler: suspend (String) -> List<SearchResult> = { emptyList() },
) : StockDataSource {
    override suspend fun getQuote(symbol: String) = quoteHandler(symbol)
    override suspend fun getChartData(symbol: String, period: TimePeriod) =
        chartHandler(symbol, period)
    override suspend fun searchStocks(query: String) = searchHandler(query)
}
