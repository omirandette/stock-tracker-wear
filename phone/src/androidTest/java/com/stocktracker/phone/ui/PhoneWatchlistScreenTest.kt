package com.stocktracker.phone.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.stocktracker.phone.testutil.ConfigurableFakeDataSource
import com.stocktracker.phone.testutil.InMemoryStockDao
import com.stocktracker.phone.ui.theme.PhoneTheme
import com.stocktracker.shared.data.local.StockEntity
import com.stocktracker.shared.data.repository.StockRepository
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class PhoneWatchlistScreenTest {

    @get:Rule val composeRule = createComposeRule()

    private val dao = InMemoryStockDao()
    private val dataSource = ConfigurableFakeDataSource()
    private val repository = StockRepository(dataSource, dao)

    @Test
    fun stockRows_displaySymbolsAndPrices() {
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
    fun emptyState_showsAddHint() {
        setScreen()
        composeRule.onNodeWithText("No stocks yet\nTap + to add one").assertIsDisplayed()
    }

    @Test
    fun positiveChange_showsText() {
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

    @Test
    fun fab_invokesOnAddClick() {
        var clicked = false
        setScreen(onAddClick = { clicked = true })
        composeRule.onNodeWithContentDescription("Add stock").performClick()
        assertTrue(clicked)
    }

    private fun setScreen(
        onStockClick: (com.stocktracker.shared.model.Stock) -> Unit = {},
        onAddClick: () -> Unit = {},
    ) {
        val vm = PhoneWatchlistViewModel(repository)
        composeRule.setContent {
            PhoneTheme {
                PhoneWatchlistScreen(
                    viewModel = vm,
                    onStockClick = onStockClick,
                    onAddClick = onAddClick,
                )
            }
        }
    }
}
