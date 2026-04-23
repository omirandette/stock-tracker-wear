package com.stocktracker.phone.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.stocktracker.phone.MainPhoneActivity
import com.stocktracker.shared.model.Stock
import com.stocktracker.shared.ui.StockColors
import com.stocktracker.shared.ui.formatChangeWithPercent
import com.stocktracker.shared.ui.formatPrice
import com.stocktracker.shared.ui.formatTimestamp

internal enum class WidgetSize { Compact, Medium, Large, Full }

internal fun pickSize(width: androidx.compose.ui.unit.Dp): WidgetSize = when {
    width < 260.dp -> WidgetSize.Compact
    width < 440.dp -> WidgetSize.Medium
    width < 600.dp -> WidgetSize.Large
    else -> WidgetSize.Full
}

internal fun rowCap(size: WidgetSize): Int = when (size) {
    WidgetSize.Compact -> 3
    WidgetSize.Medium -> 5
    WidgetSize.Large -> 10
    WidgetSize.Full -> Int.MAX_VALUE
}

private val BackgroundColor = Color(0xFF0D0D0D)
private val SurfaceColor = Color(0xFF1A1A1A)
private val OnSurface = Color(0xFFE0E0E0)
private val OnSurfaceVariant = Color(0xFF9E9E9E)

@Composable
internal fun StockWidgetContent(stocks: List<Stock>, size: WidgetSize) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(BackgroundColor)
            .padding(8.dp)
            .clickable(actionStartActivity<MainPhoneActivity>()),
    ) {
        Header(size = size, hasStocks = stocks.isNotEmpty())
        Spacer(GlanceModifier.height(4.dp))
        if (stocks.isEmpty()) {
            EmptyState()
        } else {
            val cap = rowCap(size)
            val shown = if (stocks.size > cap) stocks.take(cap) else stocks
            if (size == WidgetSize.Large || size == WidgetSize.Full) {
                LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
                    items(shown, itemId = { it.symbol.hashCode().toLong() }) { stock ->
                        StockRow(stock = stock, size = size)
                    }
                }
            } else {
                Column(modifier = GlanceModifier.fillMaxSize()) {
                    shown.forEach { stock -> StockRow(stock = stock, size = size) }
                }
            }
        }
    }
}

@Composable
private fun Header(size: WidgetSize, hasStocks: Boolean) {
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Stocks",
            style = TextStyle(
                color = androidx.glance.unit.ColorProvider(OnSurface),
                fontSize = if (size == WidgetSize.Compact) 14.sp else 16.sp,
                fontWeight = FontWeight.Medium,
            ),
        )
        Spacer(GlanceModifier.width(8.dp))
        Spacer(GlanceModifier.defaultWeight())
        if (hasStocks && size != WidgetSize.Compact) {
            Text(
                text = "↻",
                style = TextStyle(
                    color = androidx.glance.unit.ColorProvider(OnSurfaceVariant),
                    fontSize = 16.sp,
                ),
                modifier = GlanceModifier
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .clickable(actionRunCallback<RefreshAction>()),
            )
        }
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(8.dp)
            .clickable(actionStartActivity<MainPhoneActivity>()),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Add stocks in the app",
            style = TextStyle(
                color = androidx.glance.unit.ColorProvider(OnSurfaceVariant),
                fontSize = 13.sp,
            ),
        )
    }
}

@Composable
private fun StockRow(stock: Stock, size: WidgetSize) {
    val changeColor = if (stock.change >= 0) StockColors.up else StockColors.down
    val symbolSize = if (size == WidgetSize.Compact) 13.sp else 14.sp
    val priceSize = if (size == WidgetSize.Compact) 13.sp else 14.sp
    val changeSize = if (size == WidgetSize.Compact) 11.sp else 12.sp

    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = GlanceModifier.defaultWeight()) {
            Text(
                text = stock.symbol,
                style = TextStyle(
                    color = androidx.glance.unit.ColorProvider(OnSurface),
                    fontSize = symbolSize,
                    fontWeight = FontWeight.Medium,
                ),
            )
            if (size != WidgetSize.Compact) {
                Text(
                    text = formatChangeWithPercent(stock),
                    style = TextStyle(
                        color = androidx.glance.unit.ColorProvider(changeColor),
                        fontSize = changeSize,
                    ),
                )
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = formatPrice(stock.price),
                style = TextStyle(
                    color = androidx.glance.unit.ColorProvider(OnSurface),
                    fontSize = priceSize,
                ),
            )
            if (size == WidgetSize.Compact) {
                Text(
                    text = formatChangeWithPercent(stock),
                    style = TextStyle(
                        color = androidx.glance.unit.ColorProvider(changeColor),
                        fontSize = changeSize,
                    ),
                )
            } else {
                Text(
                    text = formatTimestamp(stock.lastUpdated),
                    style = TextStyle(
                        color = androidx.glance.unit.ColorProvider(OnSurfaceVariant),
                        fontSize = 11.sp,
                    ),
                )
            }
        }
    }
}
