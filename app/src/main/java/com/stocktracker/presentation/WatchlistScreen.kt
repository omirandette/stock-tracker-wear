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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.itemsIndexed
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Card
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.CompactButton
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.dialog.Dialog
import com.stocktracker.model.Stock

@Composable
fun WatchlistScreen(
    viewModel: WatchlistViewModel,
    onStockClick: (Int) -> Unit,
) {
    val stocks by viewModel.stocks.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var ticker by remember { mutableStateOf("") }

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
            Button(onClick = {
                ticker = ""
                showAddDialog = true
            }) { Text("+") }
        }
    }

    Dialog(
        showDialog = showAddDialog,
        onDismissRequest = { showAddDialog = false },
    ) {
        val focusRequester = remember { FocusRequester() }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            BasicTextField(
                value = ticker,
                onValueChange = { ticker = it.uppercase().take(10) },
                textStyle = TextStyle(
                    color = Color.White,
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center,
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (ticker.isNotBlank()) {
                            viewModel.addStock(ticker)
                            showAddDialog = false
                        }
                    },
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .focusRequester(focusRequester),
            )
        }

        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
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
                Text(text = stock.symbol, style = MaterialTheme.typography.title3)
                Text(text = "$${String.format("%.2f", stock.price)}")
            }
            Text(
                text = "${if (stock.change >= 0) "+" else ""}${String.format("%.2f", stock.change)} (${stock.changePercent})",
                color = if (stock.change >= 0) Color.Green else Color.Red,
            )
        }
    }
}
