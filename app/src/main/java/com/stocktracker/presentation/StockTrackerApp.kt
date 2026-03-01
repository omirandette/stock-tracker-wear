package com.stocktracker.presentation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.stocktracker.BuildConfig
import com.stocktracker.StockApp
import com.stocktracker.presentation.theme.StockTrackerTheme

@Composable
fun StockTrackerApp(app: StockApp) {
    val navController = rememberSwipeDismissableNavController()
    val viewModel: WatchlistViewModel = viewModel(
        factory = WatchlistViewModel.Factory(app.repository, BuildConfig.ALPHA_VANTAGE_KEY)
    )

    StockTrackerTheme {
        SwipeDismissableNavHost(navController = navController, startDestination = "watchlist") {
            composable("watchlist") {
                WatchlistScreen(
                    viewModel = viewModel,
                    onAddClick = { navController.navigate("add") },
                )
            }
            composable("add") {
                AddStockScreen(
                    onAdd = { viewModel.addStock(it) },
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}
