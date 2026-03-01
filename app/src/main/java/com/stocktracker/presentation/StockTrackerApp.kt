package com.stocktracker.presentation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.stocktracker.StockApp
import com.stocktracker.presentation.theme.StockTrackerTheme

@Composable
fun StockTrackerApp(app: StockApp) {
    val navController = rememberSwipeDismissableNavController()
    val watchlistViewModel: WatchlistViewModel = viewModel(
        factory = WatchlistViewModel.Factory(app.repository)
    )
    val detailViewModel: StockDetailViewModel = viewModel(
        factory = StockDetailViewModel.Factory(app.repository)
    )

    StockTrackerTheme {
        SwipeDismissableNavHost(navController = navController, startDestination = "watchlist") {
            composable("watchlist") {
                WatchlistScreen(
                    viewModel = watchlistViewModel,
                    onStockClick = { index -> navController.navigate("detail/$index") },
                )
            }
            composable("detail/{stockIndex}") { backStackEntry ->
                val stockIndex = backStackEntry.arguments?.getString("stockIndex")?.toIntOrNull() ?: 0
                StockDetailScreen(
                    viewModel = detailViewModel,
                    initialStockIndex = stockIndex,
                )
            }
        }
    }
}
