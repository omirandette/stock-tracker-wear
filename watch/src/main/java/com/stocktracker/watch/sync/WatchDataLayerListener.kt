package com.stocktracker.watch.sync

import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMap
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService
import com.stocktracker.shared.data.repository.StockRepository
import com.stocktracker.watch.StockApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Applies watchlist updates pushed from the phone. The payload carries only the
 * symbol list — prices are refreshed locally by the watch's own auto-refresh loop.
 */
open class WatchDataLayerListener : WearableListenerService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onDataChanged(events: DataEventBuffer) {
        // onDataChanged's buffer is short-lived; freeze symbols off the buffer before launching.
        val incomingSymbolLists = events.mapNotNull { event ->
            if (event.type != DataEvent.TYPE_CHANGED) return@mapNotNull null
            if (event.dataItem.uri.path != WATCHLIST_PATH) return@mapNotNull null
            val dataMap: DataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
            dataMap.getStringArray(KEY_SYMBOLS)?.toList().orEmpty()
        }

        if (incomingSymbolLists.isEmpty()) return

        val latest = incomingSymbolLists.last()
        val repository = repository() ?: return

        scope.launch {
            applyIncoming(repository, latest)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    protected open fun repository(): StockRepository? =
        (application as? StockApp)?.repository

    internal suspend fun applyIncoming(repository: StockRepository, incoming: List<String>) {
        val current = repository.watchAll().first().map { it.symbol }.toSet()
        val incomingSet = incoming.toSet()

        // Delete symbols the phone removed.
        (current - incomingSet).forEach { symbol ->
            try {
                repository.removeStock(symbol)
            } catch (_: Exception) {
            }
        }

        // Insert new symbols with a placeholder entity — the refresh loop backfills prices.
        (incomingSet - current).forEach { symbol ->
            try {
                repository.insertPlaceholder(symbol)
            } catch (_: Exception) {
            }
        }
    }

    companion object {
        const val WATCHLIST_PATH = "/watchlist/symbols"
        const val KEY_SYMBOLS = "symbols"
    }
}
