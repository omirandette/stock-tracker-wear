package com.stocktracker.phone.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.stocktracker.phone.testutil.ConfigurableFakeDataSource
import com.stocktracker.phone.testutil.InMemoryStockDao
import com.stocktracker.phone.ui.theme.PhoneTheme
import com.stocktracker.shared.data.repository.StockRepository
import com.stocktracker.shared.model.ChartData
import com.stocktracker.shared.model.ChartPoint
import com.stocktracker.shared.model.Stock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class PhoneStockDetailScreenTest {

    @get:Rule val composeRule = createComposeRule()

    // Chart handler returns period-scoped values that match the test stock's 1D values
    // so the header assertion stays deterministic whether chart has loaded yet or not.
    private val dataSource = ConfigurableFakeDataSource(
        chartHandler = { _, _ ->
            ChartData(
                points = listOf(ChartPoint(1L, 187.49), ChartPoint(2L, 189.84)),
                change = 2.35,
                changePercent = 1.25,
            )
        },
    )
    private val repository = StockRepository(dataSource, InMemoryStockDao())

    @Test
    fun displaysSymbolAndPrice() {
        setScreen(Stock("AAPL", 189.84, 2.35, "1.25%", 0L))
        composeRule.onNodeWithText("AAPL").assertIsDisplayed()
        composeRule.onNodeWithText("$189.84").assertIsDisplayed()
        composeRule.onNodeWithText("+2.35 (1.25%)").assertIsDisplayed()
    }

    @Test
    fun headerUsesPeriodChange_whenChartLoaded() {
        // Distinct chart change vs. stock's 1D change — the header must show
        // the period-scoped value, not the persisted 1D.
        val periodSource = ConfigurableFakeDataSource(
            chartHandler = { _, _ ->
                ChartData(
                    points = listOf(ChartPoint(1L, 50.0), ChartPoint(2L, 75.0)),
                    change = 25.0,
                    changePercent = 50.0,
                )
            },
        )
        val periodRepo = StockRepository(periodSource, InMemoryStockDao())
        val vm = PhoneStockDetailViewModel(periodRepo)
        val stock = Stock("AAPL", 75.0, 1.0, "1.35%", 0L)
        composeRule.setContent {
            PhoneTheme {
                PhoneStockDetailScreen(stock = stock, viewModel = vm, onBack = {}, onRemove = null)
            }
        }
        composeRule.waitUntil(timeoutMillis = 3_000) {
            composeRule.onAllNodesWithText("+25.00 (50.00%)")
                .fetchSemanticsNodes(atLeastOneRootRequired = false)
                .isNotEmpty()
        }
        composeRule.onNodeWithText("+25.00 (50.00%)").assertIsDisplayed()
    }

    @Test
    fun periodChip_isTappable() {
        setScreen(Stock("AAPL", 189.84, 2.35, "1.25%", 0L))
        composeRule.onNodeWithText("5D").performClick()
        composeRule.onNodeWithText("5D").assertIsDisplayed()
    }

    @Test
    fun removeButton_invokesOnRemove() {
        var removed = false
        setScreen(
            stock = Stock("AAPL", 189.84, 2.35, "1.25%", 0L),
            onRemove = { removed = true },
        )
        composeRule.onNodeWithText("Remove from watchlist").performClick()
        assertTrue(removed)
    }

    @Test
    fun noOnRemove_hidesRemoveButton() {
        setScreen(Stock("AAPL", 189.84, 2.35, "1.25%", 0L), onRemove = null)
        val nodes = composeRule.onAllNodesWithText("Remove from watchlist")
            .fetchSemanticsNodes(atLeastOneRootRequired = false)
        assertEquals(0, nodes.size)
    }

    @Test
    fun backButton_invokesOnBack_whenProvided() {
        var backClicked = false
        setScreen(
            stock = Stock("AAPL", 189.84, 2.35, "1.25%", 0L),
            onBack = { backClicked = true },
        )
        composeRule.onNodeWithContentDescription("Back").assertIsDisplayed().performClick()
        assertTrue(backClicked)
    }

    @Test
    fun noOnBack_hidesBackButton() {
        setScreen(Stock("AAPL", 189.84, 2.35, "1.25%", 0L), onBack = null)
        val nodes = composeRule.onAllNodesWithContentDescription("Back")
            .fetchSemanticsNodes(atLeastOneRootRequired = false)
        assertEquals(0, nodes.size)
    }

    private fun setScreen(
        stock: Stock,
        onRemove: (() -> Unit)? = null,
        onBack: (() -> Unit)? = null,
    ) {
        val vm = PhoneStockDetailViewModel(repository)
        composeRule.setContent {
            PhoneTheme {
                PhoneStockDetailScreen(
                    stock = stock,
                    viewModel = vm,
                    onBack = onBack,
                    onRemove = onRemove,
                )
            }
        }
    }
}
