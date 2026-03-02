package com.stocktracker.testutil

import com.stocktracker.data.local.StockDao
import com.stocktracker.data.local.StockEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class InMemoryStockDao : StockDao {
    private val stocks = MutableStateFlow<Map<String, StockEntity>>(emptyMap())

    fun seed(vararg entities: StockEntity) {
        stocks.value = entities.associateBy { it.symbol }
    }

    override fun getAll(): Flow<List<StockEntity>> =
        stocks.map { it.values.sortedBy { e -> e.symbol } }

    override suspend fun insert(stock: StockEntity) {
        stocks.value = stocks.value + (stock.symbol to stock)
    }

    override suspend fun delete(symbol: String) {
        stocks.value = stocks.value - symbol
    }
}
