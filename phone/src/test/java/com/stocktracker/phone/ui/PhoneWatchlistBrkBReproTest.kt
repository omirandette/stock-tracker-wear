@file:OptIn(
    androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class,
)

package com.stocktracker.phone.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.NavigableListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import com.stocktracker.phone.sync.WatchlistPublisher
import com.stocktracker.phone.sync.WatchlistTransport
import com.stocktracker.phone.testutil.ConfigurableFakeDataSource
import com.stocktracker.phone.testutil.InMemoryStockDao
import com.stocktracker.phone.ui.theme.PhoneTheme
import com.stocktracker.shared.data.local.StockEntity
import com.stocktracker.shared.data.repository.StockRepository
import com.stocktracker.shared.model.Stock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Reproducer for the "BRK-B added but not visible in the watchlist" bug.
 *
 * Seeds a single stock with a dash-containing symbol and renders the screen at three
 * levels of wrapping:
 *  - [brkB_rendersInBareScreen]: just `PhoneWatchlistScreen` — narrows to either render or data
 *  - [brkB_rendersInScaffoldWrap]: wrapped in `NavigableListDetailPaneScaffold` as the real app does
 *
 * A passing bare-screen test plus a failing scaffold-wrapped test pinpoints the scaffold.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w411dp-h891dp-normal-long-notround-notnight-560dpi-keyshidden-nonav")
class PhoneWatchlistBrkBReproTest {

    @get:Rule val composeRule = createComposeRule()

    private val dao = InMemoryStockDao().apply {
        seed(StockEntity("BRK-B", 467.42, -1.08, "-0.23%", 1776878411000L))
    }
    private val repository = StockRepository(ConfigurableFakeDataSource(), dao)

    @Test
    fun brkB_rendersInBareScreen() {
        val vm = PhoneWatchlistViewModel(repository)
        composeRule.setContent {
            PhoneTheme {
                PhoneWatchlistScreen(
                    viewModel = vm,
                    onStockClick = {},
                    onAddClick = {},
                )
            }
        }
        composeRule.onRoot().captureRoboImage("src/test/snapshots/roborazzi/brkb_bare.png")
        val nodes = composeRule.onAllNodesWithText("BRK-B").fetchSemanticsNodes(atLeastOneRootRequired = false)
        assertEquals("BRK-B must render in bare screen", 1, nodes.size)
    }

    @Test
    fun brkB_rendersAfterAddStockCall() {
        val freshDao = InMemoryStockDao()
        val fakeSource = ConfigurableFakeDataSource(
            quoteHandler = { sym ->
                com.stocktracker.shared.data.api.QuoteResult(sym, 467.42, -1.08, "-0.23%", 1776878411000L)
            },
        )
        val repo = StockRepository(fakeSource, freshDao)
        val vm = PhoneWatchlistViewModel(repo)
        composeRule.setContent {
            PhoneTheme {
                PhoneWatchlistScreen(vm, onStockClick = {}, onAddClick = {})
            }
        }
        vm.addStock("BRK-B")
        composeRule.waitUntil(timeoutMillis = 3_000) {
            composeRule.onAllNodesWithText("BRK-B")
                .fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty()
        }
        composeRule.onRoot().captureRoboImage("src/test/snapshots/roborazzi/brkb_addstock.png")
        val nodes = composeRule.onAllNodesWithText("BRK-B").fetchSemanticsNodes(atLeastOneRootRequired = false)
        assertEquals("BRK-B must render after addStock call", 1, nodes.size)
    }

    @Test
    fun brkB_rendersAfterSheetBasedAdd_fullAppShape() {
        val freshDao = InMemoryStockDao()
        val fakeSource = ConfigurableFakeDataSource(
            quoteHandler = { sym ->
                com.stocktracker.shared.data.api.QuoteResult(sym, 467.42, -1.08, "-0.23%", 1776878411000L)
            },
        )
        val repo = StockRepository(fakeSource, freshDao)
        val vm = PhoneWatchlistViewModel(repo)

        composeRule.setContent {
            PhoneTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navigator = rememberListDetailPaneScaffoldNavigator<Any>()
                    val stocks by vm.stocks.collectAsState()
                    var showSheet by remember { mutableStateOf(false) }

                    NavigableListDetailPaneScaffold(
                        navigator = navigator,
                        listPane = {
                            AnimatedPane {
                                PhoneWatchlistScreen(
                                    viewModel = vm,
                                    onStockClick = { s ->
                                        navigator.navigateTo(
                                            ListDetailPaneScaffoldRole.Detail,
                                            s.symbol,
                                        )
                                    },
                                    onAddClick = { showSheet = true },
                                )
                            }
                        },
                        detailPane = {
                            AnimatedPane {
                                val sym = navigator.currentDestination?.content as? String
                                val selected = sym?.let { x -> stocks.firstOrNull { it.symbol == x } }
                                if (selected != null) Text(selected.symbol)
                            }
                        },
                    )

                    if (showSheet) {
                        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                        ModalBottomSheet(
                            onDismissRequest = { showSheet = false; vm.clearSearch() },
                            sheetState = sheetState,
                        ) {
                            PhoneAddStockScreen(
                                viewModel = vm,
                                onClose = { showSheet = false; vm.clearSearch() },
                            )
                        }
                    }
                }
            }
        }

        // Mimic user flow: open sheet → addStock → sheet closes
        composeRule.runOnIdle { /* allow initial composition */ }
        vm.addStock("BRK-B")
        composeRule.waitUntil(timeoutMillis = 3_000) {
            composeRule.onAllNodesWithText("BRK-B")
                .fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty()
        }
        composeRule.onRoot().captureRoboImage("src/test/snapshots/roborazzi/brkb_fullflow.png")

        val nodes = composeRule.onAllNodesWithText("BRK-B")
            .fetchSemanticsNodes(atLeastOneRootRequired = false)
        assertEquals("BRK-B must render in full scaffold + sheet app shape", 1, nodes.size)
    }

    @Test
    fun brkB_rendersWithWatchlistPublisherActive() {
        val freshDao = InMemoryStockDao()
        val fakeSource = ConfigurableFakeDataSource(
            quoteHandler = { sym ->
                com.stocktracker.shared.data.api.QuoteResult(sym, 467.42, -1.08, "-0.23%", 1776878411000L)
            },
        )
        val repo = StockRepository(fakeSource, freshDao)
        val vm = PhoneWatchlistViewModel(repo)

        // Simulate the real StockPhoneApp.onCreate wiring — WatchlistPublisher
        // observing the same repository the VM observes, with a failing transport
        // (emulator has no paired Wear device, so Wearable.getDataClient throws).
        val publisherScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val failingTransport = object : WatchlistTransport {
            override suspend fun publish(symbols: List<String>) {
                throw RuntimeException("no paired wear device")
            }
        }
        WatchlistPublisher(repo, failingTransport, publisherScope).start()

        composeRule.setContent {
            PhoneTheme {
                PhoneWatchlistScreen(vm, onStockClick = {}, onAddClick = {})
            }
        }
        vm.addStock("BRK-B")
        composeRule.waitUntil(timeoutMillis = 3_000) {
            composeRule.onAllNodesWithText("BRK-B")
                .fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty()
        }
        composeRule.onRoot().captureRoboImage("src/test/snapshots/roborazzi/brkb_with_publisher.png")

        val nodes = composeRule.onAllNodesWithText("BRK-B")
            .fetchSemanticsNodes(atLeastOneRootRequired = false)
        assertEquals(
            "BRK-B must render even when WatchlistPublisher is running with a failing transport",
            1,
            nodes.size,
        )
        publisherScope.cancel()
    }

    @Test
    fun brkB_rendersInScaffoldWrap() {
        val vm = PhoneWatchlistViewModel(repository)
        composeRule.setContent {
            PhoneTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navigator = rememberListDetailPaneScaffoldNavigator<Any>()
                    val stocks by vm.stocks.collectAsState()
                    NavigableListDetailPaneScaffold(
                        navigator = navigator,
                        listPane = {
                            AnimatedPane {
                                PhoneWatchlistScreen(
                                    viewModel = vm,
                                    onStockClick = { s ->
                                        navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, s.symbol)
                                    },
                                    onAddClick = {},
                                )
                            }
                        },
                        detailPane = {
                            AnimatedPane {
                                val symbol = navigator.currentDestination?.content as? String
                                val selected: Stock? = symbol?.let { s -> stocks.firstOrNull { it.symbol == s } }
                                if (selected != null) {
                                    // Real app would show PhoneStockDetailScreen here.
                                }
                            }
                        },
                    )
                }
            }
        }
        composeRule.onRoot().captureRoboImage("src/test/snapshots/roborazzi/brkb_scaffold.png")
        val nodes = composeRule.onAllNodesWithText("BRK-B").fetchSemanticsNodes(atLeastOneRootRequired = false)
        assertEquals("BRK-B must render when wrapped in NavigableListDetailPaneScaffold", 1, nodes.size)
    }
}
