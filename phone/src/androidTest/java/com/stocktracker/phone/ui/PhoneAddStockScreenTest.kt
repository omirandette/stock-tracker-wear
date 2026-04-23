package com.stocktracker.phone.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.stocktracker.phone.testutil.ConfigurableFakeDataSource
import com.stocktracker.phone.testutil.InMemoryStockDao
import com.stocktracker.phone.ui.theme.PhoneTheme
import com.stocktracker.shared.data.repository.StockRepository
import com.stocktracker.shared.model.SearchResult
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class PhoneAddStockScreenTest {

    @get:Rule val composeRule = createComposeRule()

    private val dao = InMemoryStockDao()
    private val dataSource = ConfigurableFakeDataSource(
        searchHandler = { q ->
            if (q.startsWith("AA", ignoreCase = true)) {
                listOf(SearchResult("AAPL", "Apple Inc.", "NASDAQ"))
            } else {
                emptyList()
            }
        },
    )
    private val repository = StockRepository(dataSource, dao)

    @Test
    fun typingQuery_displaysMatchingResult() {
        setScreen()
        composeRule.onNode(hasSetTextAction()).performTextInput("AAPL")
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("Apple Inc. · NASDAQ")
                .fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty()
        }
        composeRule.onNodeWithText("Apple Inc. · NASDAQ").assertIsDisplayed()
    }

    @Test
    fun tappingResult_addsStockAndClosesScreen() {
        var closed = false
        setScreen(onClose = { closed = true })
        composeRule.onNode(hasSetTextAction()).performTextInput("AAPL")
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("Apple Inc. · NASDAQ")
                .fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty()
        }
        composeRule.onNodeWithText("Apple Inc. · NASDAQ").performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) { closed }
        assertTrue(closed)
    }

    private fun setScreen(onClose: () -> Unit = {}) {
        val vm = PhoneWatchlistViewModel(repository)
        composeRule.setContent {
            PhoneTheme {
                PhoneAddStockScreen(
                    viewModel = vm,
                    onClose = onClose,
                )
            }
        }
    }
}
