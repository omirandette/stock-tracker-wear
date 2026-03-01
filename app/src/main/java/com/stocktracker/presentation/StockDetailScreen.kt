package com.stocktracker.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.stocktracker.model.TimePeriod

@Composable
fun StockDetailScreen(
    viewModel: StockDetailViewModel,
    initialStockIndex: Int,
) {
    val stocks by viewModel.stocks.collectAsState()

    if (stocks.isEmpty()) return

    val verticalPagerState = rememberPagerState(
        initialPage = initialStockIndex.coerceIn(0, stocks.size - 1),
        pageCount = { stocks.size },
    )

    VerticalPager(
        state = verticalPagerState,
        modifier = Modifier.fillMaxSize(),
    ) { stockIndex ->
        val stock = stocks[stockIndex]
        val periods = TimePeriod.entries
        val horizontalPagerState = rememberPagerState(
            initialPage = 0,
            pageCount = { periods.size },
        )

        val chartData by viewModel.chartData.collectAsState()
        val isLoading by viewModel.isChartLoading.collectAsState()
        val chartError by viewModel.chartError.collectAsState()

        val currentPeriod = periods[horizontalPagerState.currentPage]
        val isCurrentPage = verticalPagerState.currentPage == stockIndex

        LaunchedEffect(stock.symbol, currentPeriod, isCurrentPage) {
            if (isCurrentPage) {
                viewModel.loadChart(stock.symbol, currentPeriod)
            }
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stock.symbol,
                style = MaterialTheme.typography.title3,
                modifier = Modifier.padding(top = 8.dp),
            )
            Text(
                text = "$${String.format("%.2f", stock.price)}",
                style = MaterialTheme.typography.body1,
            )
            Text(
                text = "${if (stock.change >= 0) "+" else ""}${String.format("%.2f", stock.change)} (${stock.changePercent})",
                color = if (stock.change >= 0) Color.Green else Color.Red,
                style = MaterialTheme.typography.caption2,
            )

            HorizontalPager(
                state = horizontalPagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) { periodIndex ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    if (periodIndex == horizontalPagerState.currentPage && isCurrentPage) {
                        when {
                            isLoading -> CircularProgressIndicator()
                            chartError != null -> Text(
                                chartError!!,
                                color = Color.Red,
                                style = MaterialTheme.typography.caption3,
                            )
                            else -> PriceChart(
                                points = chartData,
                                modifier = Modifier.fillMaxSize().padding(4.dp),
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                periods.forEachIndexed { index, period ->
                    Text(
                        text = period.label,
                        style = MaterialTheme.typography.caption3,
                        color = if (index == horizontalPagerState.currentPage) Color.White else Color.DarkGray,
                    )
                }
            }
        }
    }
}
