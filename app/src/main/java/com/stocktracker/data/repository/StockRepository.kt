package com.stocktracker.data.repository

import com.google.gson.JsonParser
import com.stocktracker.data.api.StockApiService
import com.stocktracker.data.local.StockDao
import com.stocktracker.data.local.StockEntity
import com.stocktracker.model.ChartPoint
import com.stocktracker.model.Stock
import com.stocktracker.model.TimePeriod
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class StockRepository(
    private val api: StockApiService,
    private val dao: StockDao,
) {
    fun watchAll(): Flow<List<Stock>> = dao.getAll().map { entities ->
        entities.map { it.toStock() }
    }

    suspend fun addStock(symbol: String, apiKey: String) {
        val response = api.getQuote(symbol = symbol.uppercase(), apiKey = apiKey)
        val quote = response.globalQuote ?: throw IllegalStateException("No data for $symbol")
        dao.insert(
            StockEntity(
                symbol = quote.symbol,
                price = quote.price.toDouble(),
                change = quote.change.toDouble(),
                changePercent = quote.changePercent,
                lastUpdated = System.currentTimeMillis(),
            )
        )
    }

    suspend fun refreshAll(apiKey: String) {
        // Collect current list once, then refresh each
        val currentSymbols = mutableListOf<String>()
        dao.getAll().collect { entities ->
            currentSymbols.addAll(entities.map { it.symbol })
            return@collect
        }
        for (symbol in currentSymbols) {
            try {
                addStock(symbol, apiKey)
            } catch (_: Exception) {
                // Keep stale data if refresh fails for one stock
            }
        }
    }

    suspend fun removeStock(symbol: String) {
        dao.delete(symbol)
    }

    suspend fun getChartData(
        symbol: String,
        period: TimePeriod,
        apiKey: String,
    ): List<ChartPoint> {
        val responseBody = if (period == TimePeriod.TWELVE_MONTHS) {
            api.getTimeSeries(
                function = "TIME_SERIES_WEEKLY",
                symbol = symbol,
                apiKey = apiKey,
            )
        } else {
            api.getTimeSeries(
                function = "TIME_SERIES_DAILY",
                symbol = symbol,
                apiKey = apiKey,
                outputSize = "compact",
            )
        }

        val json = JsonParser.parseString(responseBody.string()).asJsonObject
        val seriesKey = json.keySet().firstOrNull { it.contains("Time Series") }
            ?: throw IllegalStateException("No time series data for $symbol: ${json.keySet()}")
        val series = json.getAsJsonObject(seriesKey)

        val cutoff = calculateCutoff(period)
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        return series.entrySet()
            .mapNotNull { (dateStr, values) ->
                val date = formatter.parse(dateStr) ?: return@mapNotNull null
                if (date.time < cutoff) return@mapNotNull null
                val close = values.asJsonObject.get("4. close").asString.toDoubleOrNull()
                    ?: return@mapNotNull null
                ChartPoint(timestamp = date.time, price = close)
            }
            .sortedBy { it.timestamp }
    }

    private fun calculateCutoff(period: TimePeriod): Long {
        val cal = Calendar.getInstance()
        when (period) {
            TimePeriod.ONE_DAY -> cal.add(Calendar.DAY_OF_YEAR, -1)
            TimePeriod.FIVE_DAYS -> cal.add(Calendar.DAY_OF_YEAR, -7)
            TimePeriod.THREE_MONTHS -> cal.add(Calendar.MONTH, -3)
            TimePeriod.SIX_MONTHS -> cal.add(Calendar.MONTH, -6)
            TimePeriod.TWELVE_MONTHS -> cal.add(Calendar.YEAR, -1)
            TimePeriod.YTD -> {
                cal.set(Calendar.DAY_OF_YEAR, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
            }
        }
        return cal.timeInMillis
    }

    private fun StockEntity.toStock() = Stock(
        symbol = symbol,
        price = price,
        change = change,
        changePercent = changePercent,
    )
}
