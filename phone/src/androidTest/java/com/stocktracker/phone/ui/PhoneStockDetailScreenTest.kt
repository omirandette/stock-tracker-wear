package com.stocktracker.phone.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
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

    private val dataSource = ConfigurableFakeDataSource(
        chartHandler = { _, _ ->
            ChartData(
                points = listOf(ChartPoint(1L, 100.0), ChartPoint(2L, 110.0)),
                change = 10.0,
                changePercent = 10.0,
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

    private fun setScreen(stock: Stock, onRemove: (() -> Unit)? = null) {
        val vm = PhoneStockDetailViewModel(repository)
        composeRule.setContent {
            PhoneTheme {
                PhoneStockDetailScreen(
                    stock = stock,
                    viewModel = vm,
                    onRemove = onRemove,
                )
            }
        }
    }
}
