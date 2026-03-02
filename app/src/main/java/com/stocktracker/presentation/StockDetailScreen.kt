package com.stocktracker.presentation

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.stocktracker.model.TimePeriod
import kotlinx.coroutines.launch

@Composable
fun StockDetailScreen(
    viewModel: StockDetailViewModel,
    initialStockIndex: Int,
) {
    val stocks by viewModel.stocks.collectAsState()

    if (stocks.isEmpty()) return

    val stock = stocks[initialStockIndex.coerceIn(0, stocks.size - 1)]
    val periods = TimePeriod.activePeriods()

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { periods.size },
    )

    val chart by viewModel.chartData.collectAsState()
    val isLoading by viewModel.isChartLoading.collectAsState()
    val chartError by viewModel.chartError.collectAsState()

    val currentPeriod = periods[pagerState.currentPage]
    val focusRequester = remember { FocusRequester() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(stock.symbol, currentPeriod) {
        viewModel.loadChart(stock.symbol, currentPeriod)
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    VerticalPager(
        state = pagerState,
        modifier = Modifier
            .fillMaxSize()
            .onRotaryScrollEvent { event ->
                coroutineScope.launch {
                    val target = if (event.verticalScrollPixels > 0) {
                        (pagerState.currentPage + 1).coerceAtMost(periods.size - 1)
                    } else {
                        (pagerState.currentPage - 1).coerceAtLeast(0)
                    }
                    pagerState.animateScrollToPage(target)
                }
                true
            }
            .focusRequester(focusRequester)
            .focusable(),
    ) { periodIndex ->
        val period = periods[periodIndex]

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

            val periodChange = if (periodIndex == pagerState.currentPage && chart.points.size >= 2) {
                Pair(chart.change, chart.changePercent)
            } else {
                Pair(stock.change, stock.changePercent.removeSuffix("%").toDoubleOrNull() ?: 0.0)
            }

            Text(
                text = "${if (periodChange.first >= 0) "+" else ""}${String.format("%.2f", periodChange.first)} " +
                    "(${String.format("%.2f", periodChange.second)}%)",
                color = if (periodChange.first >= 0) Color.Green else Color.Red,
                style = MaterialTheme.typography.caption2,
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                if (periodIndex == pagerState.currentPage) {
                    when {
                        isLoading -> CircularProgressIndicator()
                        chartError != null -> Text(
                            chartError!!,
                            color = Color.Red,
                            style = MaterialTheme.typography.caption3,
                        )
                        else -> PriceChart(
                            points = chart.points,
                            isPositive = chart.change >= 0,
                            modifier = Modifier.fillMaxSize().padding(4.dp),
                        )
                    }
                }
            }

            Text(
                text = period.label,
                style = MaterialTheme.typography.caption3,
                color = Color.White,
                modifier = Modifier.padding(bottom = 4.dp),
            )
        }
    }
}
