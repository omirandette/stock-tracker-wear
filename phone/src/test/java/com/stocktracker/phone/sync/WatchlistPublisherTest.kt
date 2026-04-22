package com.stocktracker.phone.sync

import com.stocktracker.phone.testutil.MainDispatcherRule
import com.stocktracker.shared.data.repository.StockRepository
import com.stocktracker.shared.model.Stock
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WatchlistPublisherTest {

    @get:Rule val dispatcherRule = MainDispatcherRule()

    private class RecordingTransport : WatchlistTransport {
        val published = mutableListOf<List<String>>()
        override suspend fun publish(symbols: List<String>) {
            published += symbols
        }
    }

    private fun stock(symbol: String) = Stock(symbol, 100.0, 0.0, "0.00%", 0L)

    @Test
    fun `publishes uppercased symbols when repository emits`() = runTest {
        val repo = mockk<StockRepository>()
        val flow = MutableStateFlow(listOf(stock("aapl"), stock("googl")))
        every { repo.watchAll() } returns flow

        val transport = RecordingTransport()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
        WatchlistPublisher(repo, transport, scope).start()

        advanceTimeBy(WatchlistPublisher.DEBOUNCE_MS + 50)
        advanceUntilIdle()

        assertEquals(1, transport.published.size)
        assertEquals(listOf("AAPL", "GOOGL"), transport.published.last())
        scope.cancel()
    }

    @Test
    fun `debounces rapid edits into a single publish`() = runTest {
        val repo = mockk<StockRepository>()
        val flow = MutableStateFlow(listOf(stock("AAPL")))
        every { repo.watchAll() } returns flow

        val transport = RecordingTransport()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
        WatchlistPublisher(repo, transport, scope).start()

        flow.value = listOf(stock("AAPL"), stock("GOOGL"))
        flow.value = listOf(stock("AAPL"), stock("GOOGL"), stock("MSFT"))
        flow.value = listOf(stock("GOOGL"), stock("MSFT"))

        advanceTimeBy(WatchlistPublisher.DEBOUNCE_MS + 50)
        advanceUntilIdle()

        assertEquals(1, transport.published.size)
        assertEquals(listOf("GOOGL", "MSFT"), transport.published.last())
        scope.cancel()
    }

    @Test
    fun `skips publish when symbol list unchanged`() = runTest {
        val repo = mockk<StockRepository>()
        val flow = MutableStateFlow(listOf(stock("AAPL")))
        every { repo.watchAll() } returns flow

        val transport = RecordingTransport()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
        WatchlistPublisher(repo, transport, scope).start()

        advanceTimeBy(WatchlistPublisher.DEBOUNCE_MS + 50)
        // Re-emit the exact same list (no new symbols).
        flow.value = listOf(stock("AAPL"))
        advanceTimeBy(WatchlistPublisher.DEBOUNCE_MS + 50)
        advanceUntilIdle()

        assertEquals(1, transport.published.size)
        scope.cancel()
    }

    @Test
    fun `swallows transport failures`() = runTest {
        val repo = mockk<StockRepository>()
        val flow = MutableStateFlow(listOf(stock("AAPL")))
        every { repo.watchAll() } returns flow

        val failing = object : WatchlistTransport {
            var called = 0
            override suspend fun publish(symbols: List<String>) {
                called++
                throw RuntimeException("network down")
            }
        }
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
        WatchlistPublisher(repo, failing, scope).start()

        advanceTimeBy(WatchlistPublisher.DEBOUNCE_MS + 50)
        advanceUntilIdle()

        assertTrue(failing.called >= 1)
        scope.cancel()
    }
}
