@file:OptIn(
    androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class,
)

package com.stocktracker.phone.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.navigation.NavigableListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.stocktracker.phone.StockPhoneApp
import com.stocktracker.phone.ui.theme.PhoneTheme
import com.stocktracker.shared.model.Stock
import kotlinx.coroutines.launch

@Composable
fun StockTrackerPhoneApp(app: StockPhoneApp) {
    val watchlistViewModel: PhoneWatchlistViewModel = viewModel(
        factory = PhoneWatchlistViewModel.Factory(app.repository),
    )
    val detailViewModel: PhoneStockDetailViewModel = viewModel(
        factory = PhoneStockDetailViewModel.Factory(app.repository),
    )

    PhoneTheme {
        StockTrackerPhoneAppContent(
            stocksViewModel = watchlistViewModel,
            detailViewModel = detailViewModel,
        )
    }
}

@Composable
internal fun StockTrackerPhoneAppContent(
    stocksViewModel: PhoneWatchlistViewModel,
    detailViewModel: PhoneStockDetailViewModel,
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        val navigator = rememberListDetailPaneScaffoldNavigator<Any>()
        val stocks by stocksViewModel.stocks.collectAsState()
        var showAddSheet by remember { mutableStateOf(false) }

        val detailPaneExpanded =
            navigator.scaffoldValue[ListDetailPaneScaffoldRole.Detail] == PaneAdaptedValue.Expanded
        val listPaneExpanded =
            navigator.scaffoldValue[ListDetailPaneScaffoldRole.List] == PaneAdaptedValue.Expanded
        val showingBothPanes = detailPaneExpanded && listPaneExpanded

        // On expanded layouts (both panes visible), promote the first stock to
        // the detail destination when nothing is selected — otherwise the right
        // half of the screen is blank. Back-nav still works via the navigator.
        LaunchedEffect(showingBothPanes, stocks, navigator.currentDestination) {
            if (showingBothPanes &&
                navigator.currentDestination?.content == null &&
                stocks.isNotEmpty()
            ) {
                navigator.navigateTo(
                    ListDetailPaneScaffoldRole.Detail,
                    stocks.first().symbol,
                )
            }
        }

        NavigableListDetailPaneScaffold(
            navigator = navigator,
            listPane = {
                AnimatedPane {
                    PhoneWatchlistScreen(
                        viewModel = stocksViewModel,
                        onStockClick = { stock ->
                            navigator.navigateTo(
                                ListDetailPaneScaffoldRole.Detail,
                                stock.symbol,
                            )
                        },
                        onAddClick = { showAddSheet = true },
                    )
                }
            },
            detailPane = {
                AnimatedPane {
                    val symbol = navigator.currentDestination?.content as? String
                    val selected: Stock? = symbol?.let { s -> stocks.firstOrNull { it.symbol == s } }
                    if (selected != null) {
                        val coroutineScope = rememberCoroutineScope()
                        PhoneStockDetailScreen(
                            stock = selected,
                            viewModel = detailViewModel,
                            onBack = if (navigator.canNavigateBack()) {
                                { coroutineScope.launch { navigator.navigateBack() } }
                            } else null,
                            onRemove = {
                                stocksViewModel.removeStock(selected.symbol)
                                coroutineScope.launch { navigator.navigateBack() }
                            },
                        )
                    }
                }
            },
        )

        if (showAddSheet) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = {
                    stocksViewModel.clearSearch()
                    showAddSheet = false
                },
                sheetState = sheetState,
            ) {
                PhoneAddStockScreen(
                    viewModel = stocksViewModel,
                    onClose = {
                        stocksViewModel.clearSearch()
                        showAddSheet = false
                    },
                )
            }
        }
    }
}
