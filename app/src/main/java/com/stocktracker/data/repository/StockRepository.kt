package com.stocktracker.data.repository

import com.stocktracker.data.api.StockApiService
import com.stocktracker.data.local.StockDao
import com.stocktracker.data.local.StockEntity
import com.stocktracker.model.Stock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

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

    private fun StockEntity.toStock() = Stock(
        symbol = symbol,
        price = price,
        change = change,
        changePercent = changePercent,
    )
}
