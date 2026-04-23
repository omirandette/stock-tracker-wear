package com.stocktracker.watch.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.itemsIndexed
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Card
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.stocktracker.shared.model.Stock
import com.stocktracker.shared.ui.StockRowContent
import kotlinx.coroutines.delay

@Composable
fun WatchlistScreen(
    viewModel: WatchlistViewModel,
    onStockClick: (Int) -> Unit,
) {
    val stocks by viewModel.stocks.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(Unit) {
        while (true) {
            viewModel.refreshIfStale()
            delay(WatchlistViewModel.STALE_THRESHOLD_MS)
        }
    }

    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item {
            Text(
                text = "Stocks",
                style = MaterialTheme.typography.title3,
            )
        }

        if (isLoading) {
            item { CircularProgressIndicator() }
        }

        if (error != null) {
            item {
                Text(
                    text = error!!,
                    color = Color.Red,
                    textAlign = TextAlign.Center,
                )
            }
        }

        if (stocks.isEmpty() && !isLoading) {
            item {
                Text(
                    text = "Add stocks from the phone app",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.caption2,
                    modifier = Modifier.padding(16.dp),
                )
            }
        }

        itemsIndexed(stocks, key = { _, stock -> stock.symbol }) { index, stock ->
            StockCard(
                stock = stock,
                onClick = { onStockClick(index) },
            )
        }

        item { Spacer(modifier = Modifier.height(4.dp)) }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { viewModel.refresh() },
                    enabled = !isLoading,
                ) {
                    if (isLoading) {
                        CircularProgressIndicator()
                    } else {
                        Text("↻")
                    }
                }
            }
        }
    }
}

@Composable
private fun StockCard(stock: Stock, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        StockRowContent(
            stock = stock,
            symbolStyle = MaterialTheme.typography.title3,
            priceStyle = MaterialTheme.typography.body2,
            changeStyle = MaterialTheme.typography.caption2.copy(fontSize = 11.sp),
            timestampStyle = MaterialTheme.typography.caption3.copy(fontSize = 10.sp),
            contentColor = Color.White,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
