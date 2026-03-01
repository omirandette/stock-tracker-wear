package com.stocktracker.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.stocktracker.model.ChartPoint

@Composable
fun PriceChart(
    points: List<ChartPoint>,
    modifier: Modifier = Modifier,
) {
    if (points.size < 2) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("No data", style = MaterialTheme.typography.caption3)
        }
        return
    }

    val minPrice = points.minOf { it.price }
    val maxPrice = points.maxOf { it.price }
    val priceRange = (maxPrice - minPrice).coerceAtLeast(0.01)
    val chartColor = if (points.last().price >= points.first().price) Color.Green else Color.Red

    Column(modifier = modifier) {
        Text(
            text = "$${String.format("%.2f", maxPrice)}",
            style = MaterialTheme.typography.caption3,
            color = Color.Gray,
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

        Text(
            text = "$${String.format("%.2f", minPrice)}",
            style = MaterialTheme.typography.caption3,
            color = Color.Gray,
        )
    }
}
