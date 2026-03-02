package com.stocktracker.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.stocktracker.data.api.QuoteResult
import com.stocktracker.data.api.StockDataSource
import com.stocktracker.data.local.StockDao
import com.stocktracker.data.local.StockEntity
import com.stocktracker.data.repository.StockRepository
import com.stocktracker.model.ChartData
import com.stocktracker.model.SearchResult
import com.stocktracker.model.TimePeriod
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class WatchlistScreenTest {

    @get:Rule val composeRule = createComposeRule()

    private val dao = InMemoryStockDao()
    private val dataSource = FakeDataSource()
    private val repository = StockRepository(dataSource, dao)

    @Test
    fun stockCards_displaySymbolsAndPrices() {
        dao.seed(
            StockEntity("AAPL", 150.0, 2.0, "1.35%", System.currentTimeMillis()),
            StockEntity("GOOG", 2800.0, -15.0, "-0.53%", System.currentTimeMillis()),
        )
        setScreen()
        composeRule.onNodeWithText("AAPL").assertIsDisplayed()
        composeRule.onNodeWithText("$150.00").assertIsDisplayed()
        composeRule.onNodeWithText("GOOG").assertIsDisplayed()
        composeRule.onNodeWithText("$2800.00").assertIsDisplayed()
    }

    @Test
    fun emptyState_showsNoStocksMessage() {
        setScreen()
        composeRule.onNodeWithText("No stocks yet").assertIsDisplayed()
    }

    @Test
    fun addButton_callsOnAddClick() {
        var clicked = false
        setScreen(onAddClick = { clicked = true })
        composeRule.onNodeWithText("+").performClick()
        assertTrue(clicked)
    }

    @Test
    fun positiveChange_showsGreenText() {
        dao.seed(StockEntity("AAPL", 150.0, 2.0, "1.35%", System.currentTimeMillis()))
        setScreen()
        composeRule.onNodeWithText("+2.00 (1.35%)").assertIsDisplayed()
    }

    @Test
    fun negativeChange_showsText() {
        dao.seed(StockEntity("GOOG", 2800.0, -15.0, "-0.53%", System.currentTimeMillis()))
        setScreen()
        composeRule.onNodeWithText("-15.00 (-0.53%)").assertIsDisplayed()
    }

    private fun setScreen(
        onStockClick: (Int) -> Unit = {},
        onAddClick: () -> Unit = {},
    ) {
        val vm = WatchlistViewModel(repository)
        composeRule.setContent {
            WatchlistScreen(
                viewModel = vm,
                onStockClick = onStockClick,
                onAddClick = onAddClick,
            )
        }
    }
}

private class InMemoryStockDao : StockDao {
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

private class FakeDataSource : StockDataSource {
    override suspend fun getQuote(symbol: String) =
        QuoteResult(symbol, 100.0, 1.0, "1.00%", System.currentTimeMillis())

    override suspend fun getChartData(symbol: String, period: TimePeriod) =
        ChartData(emptyList(), 0.0, 0.0)

    override suspend fun searchStocks(query: String) = emptyList<SearchResult>()
}
