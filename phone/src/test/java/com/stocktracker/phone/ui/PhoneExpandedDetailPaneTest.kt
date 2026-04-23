@file:OptIn(
    androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class,
)

package com.stocktracker.phone.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import com.stocktracker.phone.testutil.ConfigurableFakeDataSource
import com.stocktracker.phone.testutil.InMemoryStockDao
import com.stocktracker.phone.ui.theme.PhoneTheme
import com.stocktracker.shared.data.local.StockEntity
import com.stocktracker.shared.data.repository.StockRepository
import com.stocktracker.shared.model.ChartData
import com.stocktracker.shared.model.ChartPoint
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Reproducer for issue #47: on expanded width (landscape / unfolded), the
 * `NavigableListDetailPaneScaffold` shows both panes, but the detail pane is
 * empty until the user taps a row. Expected: auto-select the first stock so
 * the detail pane renders its chart as the default.
 *
 * Expanded = WidthSizeClass.Expanded (>= 840dp). We use a 1024dp-wide
 * qualifier to guarantee the scaffold is in two-pane mode.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w1024dp-h720dp-normal-long-notround-notnight-560dpi-keyshidden-nonav")
class PhoneExpandedDetailPaneTest {

    @get:Rule val composeRule = createComposeRule()

    @Test
    fun expandedLayout_autoSelectsFirstStock_intoDetailPane() {
        val dao = InMemoryStockDao().apply {
            seed(
                StockEntity("AAPL", 189.84, 2.35, "1.25%", 1_715_000_000_000L),
                StockEntity("GOOG", 2800.00, -15.00, "-0.53%", 1_715_000_000_000L),
            )
        }
        val source = ConfigurableFakeDataSource(
            chartHandler = { _, _ ->
                ChartData(
                    points = listOf(ChartPoint(1L, 187.49), ChartPoint(2L, 189.84)),
                    change = 2.35,
                    changePercent = 1.25,
                )
            },
        )
        val repo = StockRepository(source, dao)
        val watchlistVm = PhoneWatchlistViewModel(repo)
        val detailVm = PhoneStockDetailViewModel(repo)

        // AnimatedPane keeps the Compose clock busy, so `waitUntil` would hang.
        // Drive the clock manually instead to let the VM flow + LaunchedEffect run.
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            PhoneTheme {
                StockTrackerPhoneAppContent(
                    stocksViewModel = watchlistVm,
                    detailViewModel = detailVm,
                )
            }
        }
        composeRule.mainClock.advanceTimeBy(1_000)
        composeRule.mainClock.advanceTimeBy(1_000)
        composeRule.mainClock.advanceTimeBy(1_000)

        composeRule.onRoot().captureRoboImage("src/test/snapshots/roborazzi/expanded_auto_selects_first.png")

        // On expanded layout, AAPL should appear twice: once in the list row,
        // once in the detail pane's title. Before the fix, the detail pane is
        // empty and AAPL appears only once (in the list).
        val aaplNodes = composeRule.onAllNodesWithText("AAPL")
            .fetchSemanticsNodes(atLeastOneRootRequired = false)
        assertTrue(
            "Expected AAPL to appear in both list and detail pane on expanded width " +
                "(actual count=${aaplNodes.size}). Detail pane was empty — issue #47.",
            aaplNodes.size >= 2,
        )

        // The detail pane's formatted price also needs to render — confirms
        // the full PhoneStockDetailScreen composed, not just the TopAppBar title.
        composeRule.onAllNodesWithText("$189.84")
            .fetchSemanticsNodes(atLeastOneRootRequired = false)
            .also {
                assertTrue("Detail pane price missing — screen didn't compose.", it.isNotEmpty())
            }
    }
}
