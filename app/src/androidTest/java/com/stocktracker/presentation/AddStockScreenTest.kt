package com.stocktracker.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.stocktracker.data.local.StockEntity
import com.stocktracker.data.repository.StockRepository
import com.stocktracker.model.SearchResult
import com.stocktracker.testutil.ConfigurableFakeDataSource
import com.stocktracker.testutil.InMemoryStockDao
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AddStockScreenTest {

    @get:Rule val composeRule = createComposeRule()

    private val dao = InMemoryStockDao()
    private val dataSource = ConfigurableFakeDataSource()
    private val repository = StockRepository(dataSource, dao)

    @Test
    fun searchPlaceholder_isDisplayedWhenEmpty() {
        setScreen()
        composeRule.onNodeWithText("Search stocks...").assertIsDisplayed()
    }

    @Test
    fun searchResults_displaySymbolAndName() {
        dataSource.searchHandler = { listOf(SearchResult("AAPL", "Apple Inc.", "NASDAQ")) }
        val vm = WatchlistViewModel(repository)
        composeRule.setContent { AddStockScreen(viewModel = vm, onBack = {}) }

        vm.onSearchQueryChange("AP")
        composeRule.waitUntil(3000) {
            composeRule.onAllNodes(
                androidx.compose.ui.test.hasText("AAPL")
            ).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("AAPL").assertIsDisplayed()
        composeRule.onNodeWithText("Apple Inc.").assertIsDisplayed()
    }

    @Test
    fun chipClick_addsStockAndCallsOnBack() = runTest {
        dataSource.searchHandler = { listOf(SearchResult("AAPL", "Apple Inc.", "NASDAQ")) }
        var backCalled = false
        val vm = WatchlistViewModel(repository)

        composeRule.setContent { AddStockScreen(viewModel = vm, onBack = { backCalled = true }) }

        vm.onSearchQueryChange("AP")
        composeRule.waitUntil(3000) {
            composeRule.onAllNodes(
                androidx.compose.ui.test.hasText("AAPL")
            ).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("AAPL").performClick()
        composeRule.waitForIdle()

        assertTrue("onBack should be called", backCalled)
        val stocks = dao.getAll().first()
        assertTrue("DAO should contain AAPL", stocks.any { it.symbol == "AAPL" })
    }

    @Test
    fun noResults_showsNoResultsText() {
        dataSource.searchHandler = { emptyList() }
        val vm = WatchlistViewModel(repository)
        composeRule.setContent { AddStockScreen(viewModel = vm, onBack = {}) }

        vm.onSearchQueryChange("XX")
        composeRule.waitUntil(3000) {
            composeRule.onAllNodes(
                androidx.compose.ui.test.hasText("No results")
            ).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("No results").assertIsDisplayed()
    }

    private fun setScreen(onBack: () -> Unit = {}) {
        val vm = WatchlistViewModel(repository)
        composeRule.setContent { AddStockScreen(viewModel = vm, onBack = onBack) }
    }
}
