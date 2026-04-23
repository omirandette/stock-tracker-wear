package com.stocktracker.watch.sync

import com.stocktracker.shared.data.repository.StockRepository
import com.stocktracker.shared.model.Stock
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * Unit coverage of the wipe-and-replace apply logic. When the incoming symbol
 * set differs from Room's current set, the whole watch list is cleared and
 * re-inserted as placeholders; the auto-refresh loop backfills prices.
 * Service-level coverage (DataEventBuffer → onDataChanged) is manual E2E per
 * CLAUDE.md.
 */
class WatchDataLayerListenerTest {

    private fun stock(symbol: String) = Stock(symbol, 0.0, 0.0, "0.00%", 0L)

    @Test
    fun `wipes and reinserts when a new symbol arrives`() = runTest {
        val repo = mockk<StockRepository>()
        every { repo.watchAll() } returns flowOf(listOf(stock("AAPL")))
        coEvery { repo.insertPlaceholder(any()) } returns Unit
        coEvery { repo.removeStock(any()) } returns Unit

        WatchDataLayerListener().applyIncoming(repo, listOf("AAPL", "GOOGL", "MSFT"))

        coVerify(exactly = 1) { repo.removeStock("AAPL") }
        coVerify(exactly = 1) { repo.insertPlaceholder("AAPL") }
        coVerify(exactly = 1) { repo.insertPlaceholder("GOOGL") }
        coVerify(exactly = 1) { repo.insertPlaceholder("MSFT") }
    }

    @Test
    fun `wipes and reinserts when a symbol is removed`() = runTest {
        val repo = mockk<StockRepository>()
        every { repo.watchAll() } returns flowOf(listOf(stock("AAPL"), stock("GOOGL")))
        coEvery { repo.removeStock(any()) } returns Unit
        coEvery { repo.insertPlaceholder(any()) } returns Unit

        WatchDataLayerListener().applyIncoming(repo, listOf("AAPL"))

        coVerify(exactly = 1) { repo.removeStock("AAPL") }
        coVerify(exactly = 1) { repo.removeStock("GOOGL") }
        coVerify(exactly = 1) { repo.insertPlaceholder("AAPL") }
        coVerify(exactly = 0) { repo.insertPlaceholder("GOOGL") }
    }

    @Test
    fun `no-op when incoming set matches Room set, order-insensitive`() = runTest {
        val repo = mockk<StockRepository>()
        every { repo.watchAll() } returns flowOf(listOf(stock("AAPL"), stock("GOOGL")))
        coEvery { repo.removeStock(any()) } returns Unit
        coEvery { repo.insertPlaceholder(any()) } returns Unit

        // Same symbols, reversed order — should still be a no-op.
        WatchDataLayerListener().applyIncoming(repo, listOf("GOOGL", "AAPL"))

        coVerify(exactly = 0) { repo.removeStock(any()) }
        coVerify(exactly = 0) { repo.insertPlaceholder(any()) }
    }

    @Test
    fun `empty incoming list removes all existing symbols`() = runTest {
        val repo = mockk<StockRepository>()
        every { repo.watchAll() } returns flowOf(listOf(stock("AAPL"), stock("GOOGL")))
        coEvery { repo.removeStock(any()) } returns Unit
        coEvery { repo.insertPlaceholder(any()) } returns Unit

        WatchDataLayerListener().applyIncoming(repo, emptyList())

        coVerify(exactly = 1) { repo.removeStock("AAPL") }
        coVerify(exactly = 1) { repo.removeStock("GOOGL") }
        coVerify(exactly = 0) { repo.insertPlaceholder(any()) }
    }

    @Test
    fun `per-symbol insert failure does not abort other inserts`() = runTest {
        val repo = mockk<StockRepository>()
        every { repo.watchAll() } returns flowOf(emptyList())
        coEvery { repo.insertPlaceholder("GOOGL") } throws RuntimeException("boom")
        coEvery { repo.insertPlaceholder("MSFT") } returns Unit

        WatchDataLayerListener().applyIncoming(repo, listOf("GOOGL", "MSFT"))

        coVerify(exactly = 1) { repo.insertPlaceholder("GOOGL") }
        coVerify(exactly = 1) { repo.insertPlaceholder("MSFT") }
    }
}
