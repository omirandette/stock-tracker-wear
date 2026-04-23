package com.stocktracker.phone.sync

import com.stocktracker.shared.data.repository.StockRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

/**
 * Observes the repository's watchlist and pushes symbol changes to the paired watch
 * through the injected [WatchlistTransport]. Debounces so rapid edits coalesce into
 * a single Data Layer write.
 */
class WatchlistPublisher(
    private val repository: StockRepository,
    private val transport: WatchlistTransport,
    private val scope: CoroutineScope,
) {
    @OptIn(FlowPreview::class)
    fun start() {
        repository.watchAll()
            .map { stocks -> stocks.map { it.symbol.uppercase() } }
            .distinctUntilChanged()
            .debounce(DEBOUNCE_MS)
            .onEach { symbols ->
                try {
                    transport.publish(symbols)
                } catch (_: Exception) {
                    // Swallow transport failures; Data Layer retries, and Auto Backup is the fallback.
                }
            }
            .launchIn(scope)
    }

    companion object {
        const val DEBOUNCE_MS = 500L
    }
}
