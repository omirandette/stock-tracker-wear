package com.stocktracker.data.api.yahoo

import com.stocktracker.data.api.QuoteResult
import com.stocktracker.data.api.StockDataSource
import com.stocktracker.model.ChartPoint
import com.stocktracker.model.SearchResult
import com.stocktracker.model.TimePeriod

private val EQUITY_TYPES = setOf("EQUITY", "ETF")

class YahooFinanceDataSource(
    private val api: YahooChartApi,
    private val searchApi: YahooSearchApi,
) : StockDataSource {

    override suspend fun getQuote(symbol: String): QuoteResult {
        val response = api.getChart(symbol, range = "1d", interval = "5m")
        val result = response.chart.result?.firstOrNull()
            ?: throw IllegalStateException(
                response.chart.error?.description ?: "No data for $symbol"
            )
        val meta = result.meta
        val change = meta.regularMarketPrice - meta.previousClose
        val changePct = if (meta.previousClose != 0.0) {
            (change / meta.previousClose) * 100
        } else {
            0.0
        }
        return QuoteResult(
            symbol = meta.symbol,
            price = meta.regularMarketPrice,
            change = change,
            changePercent = String.format("%.2f%%", changePct),
        )
    }

    override suspend fun getChartData(symbol: String, period: TimePeriod): List<ChartPoint> {
        val response = api.getChart(symbol, range = period.yahooRange, interval = period.yahooInterval)
        val result = response.chart.result?.firstOrNull()
            ?: throw IllegalStateException(
                response.chart.error?.description ?: "No chart data for $symbol"
            )
        val timestamps = result.timestamp ?: return emptyList()
        val closes = result.indicators?.quote?.firstOrNull()?.close ?: return emptyList()

        return timestamps.zip(closes).mapNotNull { (ts, close) ->
            close?.let { ChartPoint(timestamp = ts * 1000, price = it) }
        }
    }

    override suspend fun searchStocks(query: String): List<SearchResult> {
        val response = searchApi.search(query)
        return response.quotes
            .filter { it.quoteType in EQUITY_TYPES }
            .take(5)
            .map { quote ->
                SearchResult(
                    symbol = quote.symbol,
                    name = quote.longname ?: quote.shortname ?: quote.symbol,
                    exchange = quote.exchDisp ?: "",
                )
            }
    }
}
