@file:OptIn(androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi::class)

package com.stocktracker.phone.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.NavigableListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.stocktracker.phone.StockPhoneApp
import com.stocktracker.phone.ui.theme.PhoneTheme
import com.stocktracker.shared.model.Stock

@Composable
fun StockTrackerPhoneApp(app: StockPhoneApp) {
    val watchlistViewModel: PhoneWatchlistViewModel = viewModel(
        factory = PhoneWatchlistViewModel.Factory(app.repository),
    )
    val detailViewModel: PhoneStockDetailViewModel = viewModel(
        factory = PhoneStockDetailViewModel.Factory(app.repository),
    )

    PhoneTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            val navigator = rememberListDetailPaneScaffoldNavigator<Any>()
            val stocks by watchlistViewModel.stocks.collectAsState()

            NavigableListDetailPaneScaffold(
                navigator = navigator,
                listPane = {
                    AnimatedPane {
                        PhoneWatchlistScreen(
                            viewModel = watchlistViewModel,
                            onStockClick = { stock ->
                                navigator.navigateTo(
                                    ListDetailPaneScaffoldRole.Detail,
                                    stock.symbol,
                                )
                            },
                        )
                    }
                },
                detailPane = {
                    AnimatedPane {
                        val symbol = navigator.currentDestination?.content as? String
                        val selected: Stock? = symbol?.let { s -> stocks.firstOrNull { it.symbol == s } }
                        if (selected != null) {
                            PhoneStockDetailScreen(
                                stock = selected,
                                viewModel = detailViewModel,
                            )
                        }
                    }
                },
            )
        }
    }
}
