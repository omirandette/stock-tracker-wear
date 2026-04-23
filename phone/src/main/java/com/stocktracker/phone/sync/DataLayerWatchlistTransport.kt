package com.stocktracker.phone.sync

import android.content.Context
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.tasks.await

/**
 * Production transport: writes the watchlist to DataClient at path /watchlist/symbols.
 * The watch's WearableListenerService receives the update and syncs Room locally.
 */
class DataLayerWatchlistTransport(private val context: Context) : WatchlistTransport {
    override suspend fun publish(symbols: List<String>) {
        val request = PutDataMapRequest.create(WATCHLIST_PATH).apply {
            dataMap.putStringArray(KEY_SYMBOLS, symbols.toTypedArray())
            dataMap.putLong(KEY_VERSION, System.currentTimeMillis())
        }.asPutDataRequest().setUrgent()
        Wearable.getDataClient(context).putDataItem(request).await()
    }

    companion object {
        const val WATCHLIST_PATH = "/watchlist/symbols"
        const val KEY_SYMBOLS = "symbols"
        const val KEY_VERSION = "version"
    }
}
