package com.stocktracker.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stocks")
data class StockEntity(
    @PrimaryKey val symbol: String,
    val price: Double,
    val change: Double,
    val changePercent: String,
    val lastUpdated: Long,
)
