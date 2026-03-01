package com.stocktracker.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text

@Composable
fun AddStockScreen(
    viewModel: WatchlistViewModel,
    onBack: () -> Unit,
) {
    val query by viewModel.searchQuery.collectAsState()
    val results by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val focusRequester = remember { FocusRequester() }

    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        item {
            BasicTextField(
                value = query,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                textStyle = TextStyle(
                    color = Color.White,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                decorationBox = { innerTextField ->
                    if (query.isEmpty()) {
                        Text(
                            text = "Search stocks...",
                            style = TextStyle(
                                color = Color.Gray,
                                fontSize = 20.sp,
                                textAlign = TextAlign.Center,
                            ),
                        )
                    }
                    innerTextField()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .focusRequester(focusRequester),
            )
        }

        if (isSearching) {
            item { CircularProgressIndicator() }
        }

        if (!isSearching && query.length >= 2 && results.isEmpty()) {
            item {
                Text(
                    text = "No results",
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                )
            }
        }

        items(results, key = { it.symbol }) { result ->
            Chip(
                onClick = {
                    viewModel.addStock(result.symbol)
                    viewModel.clearSearch()
                    onBack()
                },
                label = { Text(result.symbol, style = MaterialTheme.typography.title3) },
                secondaryLabel = { Text(result.name, maxLines = 1) },
                colors = ChipDefaults.secondaryChipColors(),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}
