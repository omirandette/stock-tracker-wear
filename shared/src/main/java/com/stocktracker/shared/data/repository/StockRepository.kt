package com.stocktracker.shared.data.repository

import com.stocktracker.shared.data.api.StockDataSource
import com.stocktracker.shared.data.local.StockDao
import com.stocktracker.shared.data.local.StockEntity
import com.stocktracker.shared.model.ChartData
import com.stocktracker.shared.model.SearchResult
import com.stocktracker.shared.model.Stock
import com.stocktracker.shared.model.TimePeriod
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class StockRepository(
    private val dataSource: StockDataSource,
    private val dao: StockDao,
) {
    fun watchAll(): Flow<List<Stock>> = dao.getAll().map { entities ->
        entities.map { it.toStock() }
    }

    suspend fun addStock(symbol: String) {
        val quote = dataSource.getQuote(symbol.uppercase())
        dao.insert(
            StockEntity(
                symbol = quote.symbol,
                price = quote.price,
                change = quote.change,
                changePercent = quote.changePercent,
                lastUpdated = quote.lastUpdated,
            )
        )
    }

    suspend fun refreshAll() {
        val currentSymbols = dao.getAll().first().map { it.symbol }
        for (symbol in currentSymbols) {
            try {
                addStock(symbol)
            } catch (_: Exception) {
                // Keep stale data if refresh fails for one stock
            }
        }
    }

    suspend fun removeStock(symbol: String) {
        dao.delete(symbol)
    }

    /**
     * Inserts a symbol with placeholder price/change values without hitting the API.
     * Used by the watch's sync listener; the auto-refresh loop fills in real data shortly after.
     */
    suspend fun insertPlaceholder(symbol: String) {
        dao.insert(
            StockEntity(
                symbol = symbol.uppercase(),
                price = 0.0,
                change = 0.0,
                changePercent = "0.00%",
                lastUpdated = 0L,
            )
        )
    }

    suspend fun getChartData(symbol: String, period: TimePeriod): ChartData {
        return dataSource.getChartData(symbol, period)
    }

    suspend fun searchStocks(query: String): List<SearchResult> {
        return dataSource.searchStocks(query)
    }

    private fun StockEntity.toStock() = Stock(
        symbol = symbol,
        price = price,
        change = change,
        changePercent = changePercent,
        lastUpdated = lastUpdated,
    )
}
