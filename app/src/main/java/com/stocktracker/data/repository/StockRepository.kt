package com.stocktracker.data.repository

import com.stocktracker.data.api.StockDataSource
import com.stocktracker.data.local.StockDao
import com.stocktracker.data.local.StockEntity
import com.stocktracker.model.ChartData
import com.stocktracker.model.SearchResult
import com.stocktracker.model.Stock
import com.stocktracker.model.TimePeriod
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
