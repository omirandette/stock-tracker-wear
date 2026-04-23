package com.stocktracker.phone.sync

/**
 * Platform-neutral adapter for publishing the watchlist symbol list. The real implementation
 * writes a DataItem via the Wearable Data Layer; tests inject a fake that captures calls.
 */
interface WatchlistTransport {
    suspend fun publish(symbols: List<String>)
}
