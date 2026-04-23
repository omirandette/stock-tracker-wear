@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.stocktracker.phone.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.stocktracker.shared.model.Stock
import com.stocktracker.shared.model.TimePeriod
import com.stocktracker.shared.ui.PriceChart
import com.stocktracker.shared.ui.StockColors
import com.stocktracker.shared.ui.formatChangeWithPercent
import com.stocktracker.shared.ui.formatPrice

@Composable
fun PhoneStockDetailScreen(
    stock: Stock,
    viewModel: PhoneStockDetailViewModel,
    onBack: (() -> Unit)? = null,
    onRemove: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    var period by remember(stock.symbol) { mutableStateOf(TimePeriod.ONE_DAY) }
    val chart by viewModel.chartData.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(stock.symbol, period) {
        viewModel.loadChart(stock.symbol, period)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stock.symbol) },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        },
    ) { padding ->
        // When chart data for the selected period is loaded, the header shows the
        // period-scoped change (e.g. 1Y vs today). Before that, fall back to the
        // 1D change from the persisted Stock row so the header never appears blank.
        val periodChange: Double
        val periodChangePercent: String
        if (chart.points.size >= 2) {
            periodChange = chart.change
            periodChangePercent = "%.2f%%".format(chart.changePercent)
        } else {
            periodChange = stock.change
            periodChangePercent = stock.changePercent
        }
        val sign = if (periodChange >= 0) "+" else ""

        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text(
                text = formatPrice(stock.price),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "$sign${"%.2f".format(periodChange)} ($periodChangePercent)",
                color = if (periodChange >= 0) StockColors.up else StockColors.down,
                style = MaterialTheme.typography.bodyMedium,
            )

            Spacer(Modifier.height(16.dp))

            Box(
                modifier = Modifier.fillMaxWidth().height(220.dp),
                contentAlignment = Alignment.Center,
            ) {
                when {
                    isLoading -> CircularProgressIndicator()
                    error != null -> Text(error!!, color = MaterialTheme.colorScheme.error)
                    chart.points.size < 2 -> Text(
                        "No data",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    else -> PriceChart(
                        points = chart.points,
                        isPositive = chart.change >= 0,
                        labelStyle = MaterialTheme.typography.bodySmall
                            .copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(TimePeriod.activePeriods()) { p ->
                    FilterChip(
                        selected = p == period,
                        onClick = { period = p },
                        label = { Text(p.label) },
                        colors = FilterChipDefaults.filterChipColors(),
                    )
                }
            }

            if (onRemove != null) {
                Spacer(Modifier.height(24.dp))
                OutlinedButton(
                    onClick = onRemove,
                    modifier = Modifier.align(Alignment.End),
                ) {
                    Text("Remove from watchlist")
                }
            }
        }
    }
}
