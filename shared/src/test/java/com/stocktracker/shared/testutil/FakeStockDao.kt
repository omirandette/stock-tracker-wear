package com.stocktracker.shared.testutil

import com.stocktracker.shared.data.local.StockDao
import com.stocktracker.shared.data.local.StockEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeStockDao : StockDao {
    private val stocks = MutableStateFlow<Map<String, StockEntity>>(emptyMap())

    override fun getAll(): Flow<List<StockEntity>> =
        stocks.map { it.values.sortedBy { e -> e.symbol } }

    override suspend fun insert(stock: StockEntity) {
        stocks.value = stocks.value + (stock.symbol to stock)
    }

    override suspend fun delete(symbol: String) {
        stocks.value = stocks.value - symbol
    }
}
