package com.stocktracker.model

data class Stock(
    val symbol: String,
    val price: Double,
    val change: Double,
    val changePercent: String,
    val lastUpdated: Long,
)
