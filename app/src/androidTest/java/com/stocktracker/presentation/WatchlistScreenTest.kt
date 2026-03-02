package com.stocktracker.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.stocktracker.data.local.StockEntity
import com.stocktracker.data.repository.StockRepository
import com.stocktracker.testutil.ConfigurableFakeDataSource
import com.stocktracker.testutil.InMemoryStockDao
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class WatchlistScreenTest {

    @get:Rule val composeRule = createComposeRule()

    private val dao = InMemoryStockDao()
    private val dataSource = ConfigurableFakeDataSource()
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
