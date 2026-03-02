package com.stocktracker.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.stocktracker.data.local.StockEntity
import com.stocktracker.data.repository.StockRepository
import com.stocktracker.model.ChartData
import com.stocktracker.model.ChartPoint
import com.stocktracker.testutil.ConfigurableFakeDataSource
import com.stocktracker.testutil.InMemoryStockDao
import org.junit.Rule
import org.junit.Test

class StockDetailScreenTest {

    @get:Rule val composeRule = createComposeRule()

    private val dao = InMemoryStockDao()
    private val dataSource = ConfigurableFakeDataSource()
    private val repository = StockRepository(dataSource, dao)

    @Test
    fun displaysStockSymbolAndPrice() {
        dao.seed(StockEntity("AAPL", 150.0, 2.0, "1.35%", System.currentTimeMillis()))
        dataSource.chartHandler = { _, _ ->
            ChartData(
                listOf(ChartPoint(1000L, 148.0), ChartPoint(2000L, 150.0)),
                2.0, 1.35,
            )
        }
        setScreen(initialStockIndex = 0)
        composeRule.onNodeWithText("AAPL").assertIsDisplayed()
        composeRule.onNodeWithText("$150.00").assertIsDisplayed()
    }

    @Test
    fun emptyStocks_rendersWithoutCrash() {
        // No stocks seeded — tests the `if (stocks.isEmpty()) return` guard
        setScreen(initialStockIndex = 0)
        composeRule.waitForIdle()
        // No crash = pass
    }

    @Test
    fun outOfBoundsIndex_coercesToValidRange() {
        dao.seed(StockEntity("AAPL", 150.0, 2.0, "1.35%", System.currentTimeMillis()))
        dataSource.chartHandler = { _, _ ->
            ChartData(
                listOf(ChartPoint(1000L, 148.0), ChartPoint(2000L, 150.0)),
                2.0, 1.35,
            )
        }
        setScreen(initialStockIndex = 5)
        composeRule.onNodeWithText("AAPL").assertIsDisplayed()
    }

    @Test
    fun periodLabel_isDisplayed() {
        dao.seed(StockEntity("AAPL", 150.0, 2.0, "1.35%", System.currentTimeMillis()))
        dataSource.chartHandler = { _, _ ->
            ChartData(
                listOf(ChartPoint(1000L, 148.0), ChartPoint(2000L, 150.0)),
                2.0, 1.35,
            )
        }
        setScreen(initialStockIndex = 0)
        composeRule.onNodeWithText("1D").assertIsDisplayed()
    }

    @Test
    fun chartError_displaysErrorMessage() {
        dao.seed(StockEntity("AAPL", 150.0, 2.0, "1.35%", System.currentTimeMillis()))
        dataSource.chartHandler = { _, _ -> throw RuntimeException("network error") }
        setScreen(initialStockIndex = 0)
        composeRule.waitUntil(3000) {
            composeRule.onAllNodes(
                androidx.compose.ui.test.hasText("Failed to load chart")
            ).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Failed to load chart").assertIsDisplayed()
    }

    private fun setScreen(initialStockIndex: Int) {
        val vm = StockDetailViewModel(repository)
        composeRule.setContent {
            StockDetailScreen(viewModel = vm, initialStockIndex = initialStockIndex)
        }
    }
}
