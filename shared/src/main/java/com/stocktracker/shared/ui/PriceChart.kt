package com.stocktracker.shared.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.stocktracker.shared.model.ChartPoint

@Composable
fun PriceChart(
    points: List<ChartPoint>,
    isPositive: Boolean,
    modifier: Modifier = Modifier,
    labelStyle: TextStyle = TextStyle.Default,
) {
    if (points.size < 2) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            BasicText(text = "No data", style = labelStyle)
        }
        return
    }

    val minPrice = points.minOf { it.price }
    val maxPrice = points.maxOf { it.price }
    val priceRange = (maxPrice - minPrice).coerceAtLeast(0.01)
    val chartColor = if (isPositive) StockColors.up else StockColors.down

    Column(modifier = modifier) {
        BasicText(
            text = "$${"%.2f".format(maxPrice)}",
            style = labelStyle.copy(color = StockColors.neutral),
        )

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 4.dp),
        ) {
            val stepX = size.width / (points.size - 1).toFloat()
            val path = Path()

            points.forEachIndexed { index, point ->
                val x = index * stepX
                val y = size.height - ((point.price - minPrice) / priceRange * size.height).toFloat()

                if (index == 0) path.moveTo(x, y)
                else path.lineTo(x, y)
            }

            drawPath(
                path = path,
                color = chartColor,
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round),
            )
        }

        BasicText(
            text = "$${"%.2f".format(minPrice)}",
            style = labelStyle.copy(color = StockColors.neutral),
        )
    }
}
