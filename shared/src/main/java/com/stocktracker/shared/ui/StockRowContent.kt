package com.stocktracker.shared.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import com.stocktracker.shared.model.Stock

@Composable
fun StockRowContent(
    stock: Stock,
    symbolStyle: TextStyle,
    priceStyle: TextStyle,
    changeStyle: TextStyle,
    timestampStyle: TextStyle,
    modifier: Modifier = Modifier,
) {
    val changeColor = if (stock.change >= 0) StockColors.up else StockColors.down
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            BasicText(text = stock.symbol, style = symbolStyle)
            BasicText(text = formatPrice(stock.price), style = priceStyle)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BasicText(
                text = formatChangeWithPercent(stock),
                style = changeStyle.copy(color = changeColor),
            )
            BasicText(
                text = formatTimestamp(stock.lastUpdated),
                style = timestampStyle.copy(color = StockColors.neutral),
            )
        }
    }
}
