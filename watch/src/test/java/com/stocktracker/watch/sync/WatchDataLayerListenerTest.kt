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
 * Unit-level coverage of the diff logic that applies an incoming symbol list to Room.
 * Service-level testing (DataEventBuffer → onDataChanged → dispatch) is covered by manual
 * end-to-end verification with paired emulators per CLAUDE.md.
 */
class WatchDataLayerListenerTest {

    private fun stock(symbol: String) = Stock(symbol, 0.0, 0.0, "0.00%", 0L)

    @Test
    fun `inserts placeholder for symbols not yet in Room`() = runTest {
        val repo = mockk<StockRepository>()
        every { repo.watchAll() } returns flowOf(listOf(stock("AAPL")))
        coEvery { repo.insertPlaceholder(any()) } returns Unit
        coEvery { repo.removeStock(any()) } returns Unit

        WatchDataLayerListener().applyIncoming(repo, listOf("AAPL", "GOOGL", "MSFT"))

        coVerify(exactly = 1) { repo.insertPlaceholder("GOOGL") }
        coVerify(exactly = 1) { repo.insertPlaceholder("MSFT") }
        coVerify(exactly = 0) { repo.insertPlaceholder("AAPL") }
    }

    @Test
    fun `removes symbols that are no longer in the incoming list`() = runTest {
        val repo = mockk<StockRepository>()
        every { repo.watchAll() } returns flowOf(listOf(stock("AAPL"), stock("GOOGL")))
        coEvery { repo.removeStock(any()) } returns Unit
        coEvery { repo.insertPlaceholder(any()) } returns Unit

        WatchDataLayerListener().applyIncoming(repo, listOf("AAPL"))

        coVerify(exactly = 1) { repo.removeStock("GOOGL") }
        coVerify(exactly = 0) { repo.removeStock("AAPL") }
    }

    @Test
    fun `no-op when incoming list matches Room state`() = runTest {
        val repo = mockk<StockRepository>()
        every { repo.watchAll() } returns flowOf(listOf(stock("AAPL"), stock("GOOGL")))
        coEvery { repo.removeStock(any()) } returns Unit
        coEvery { repo.insertPlaceholder(any()) } returns Unit

        WatchDataLayerListener().applyIncoming(repo, listOf("AAPL", "GOOGL"))

        coVerify(exactly = 0) { repo.insertPlaceholder(any()) }
        coVerify(exactly = 0) { repo.removeStock(any()) }
    }

    @Test
    fun `empty incoming list removes all existing symbols`() = runTest {
        val repo = mockk<StockRepository>()
        every { repo.watchAll() } returns flowOf(listOf(stock("AAPL"), stock("GOOGL")))
        coEvery { repo.removeStock(any()) } returns Unit

        WatchDataLayerListener().applyIncoming(repo, emptyList())

        coVerify(exactly = 1) { repo.removeStock("AAPL") }
        coVerify(exactly = 1) { repo.removeStock("GOOGL") }
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
