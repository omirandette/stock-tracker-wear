@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.stocktracker.phone.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.stocktracker.phone.StockPhoneApp
import com.stocktracker.phone.ui.theme.PhoneTheme

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
        var selectedSymbol by rememberSaveable { mutableStateOf<String?>(null) }
        var showAddSheet by rememberSaveable { mutableStateOf(false) }
        val stocks by stocksViewModel.stocks.collectAsState()
        val selected = selectedSymbol?.let { sym -> stocks.firstOrNull { it.symbol == sym } }

        if (selected == null) {
            PhoneWatchlistScreen(
                viewModel = stocksViewModel,
                onStockClick = { selectedSymbol = it.symbol },
                onAddClick = { showAddSheet = true },
            )
        } else {
            PhoneStockDetailScreen(
                stock = selected,
                viewModel = detailViewModel,
                onBack = { selectedSymbol = null },
                onRemove = {
                    stocksViewModel.removeStock(selected.symbol)
                    selectedSymbol = null
                },
            )
        }

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
