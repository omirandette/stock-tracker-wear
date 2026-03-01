package com.stocktracker.presentation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.itemsIndexed
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Card
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.CompactButton
import androidx.wear.compose.material.Text
import com.stocktracker.model.Stock

@Composable
fun WatchlistScreen(
    viewModel: WatchlistViewModel,
    onAddClick: () -> Unit,
    onStockClick: (Int) -> Unit,
) {
    val stocks by viewModel.stocks.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item {
            Text(
                text = "Stocks",
                style = androidx.wear.compose.material.MaterialTheme.typography.title3,
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
                    text = "No stocks yet",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp),
                )
            }
        }

        itemsIndexed(stocks, key = { _, stock -> stock.symbol }) { index, stock ->
            StockCard(
                stock = stock,
                onClick = { onStockClick(index) },
                onLongClick = { viewModel.removeStock(stock.symbol) },
            )
        }

        item { Spacer(modifier = Modifier.height(4.dp)) }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onAddClick) { Text("+") }
                CompactButton(onClick = { viewModel.refresh() }) { Text("R") }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun StockCard(stock: Stock, onClick: () -> Unit, onLongClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick,
                ),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(text = stock.symbol, style = androidx.wear.compose.material.MaterialTheme.typography.title3)
                Text(text = "$${String.format("%.2f", stock.price)}")
            }
            Text(
                text = "${if (stock.change >= 0) "+" else ""}${String.format("%.2f", stock.change)} (${stock.changePercent})",
                color = if (stock.change >= 0) Color.Green else Color.Red,
            )
        }
    }
}
