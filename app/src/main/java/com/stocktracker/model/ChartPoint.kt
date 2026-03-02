package com.stocktracker.model

data class ChartPoint(
    val timestamp: Long,
    val price: Double,
)

data class ChartData(
    val points: List<ChartPoint>,
    val change: Double,
    val changePercent: Double,
)
