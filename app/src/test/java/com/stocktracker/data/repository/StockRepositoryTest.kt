package com.stocktracker.data.repository

import com.stocktracker.data.api.StockDataSource
import com.stocktracker.model.ChartPoint
import com.stocktracker.model.TimePeriod
import com.stocktracker.testutil.FakeStockDao
import com.stocktracker.testutil.quoteResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StockRepositoryTest {

    private val dataSource = mockk<StockDataSource>()
    private val dao = FakeStockDao()
    private val repo = StockRepository(dataSource, dao)

    @Test
    fun `watchAll maps entities to Stock models`() = runTest {
        coEvery { dataSource.getQuote("AAPL") } returns quoteResult()
        repo.addStock("AAPL")
        val stocks = repo.watchAll().first()
        assertEquals(1, stocks.size)
        assertEquals("AAPL", stocks[0].symbol)
    }

    @Test
    fun `addStock uppercases symbol`() = runTest {
        coEvery { dataSource.getQuote("AAPL") } returns quoteResult()
        repo.addStock("aapl")
        coVerify { dataSource.getQuote("AAPL") }
    }

    @Test
    fun `removeStock deletes from dao`() = runTest {
        coEvery { dataSource.getQuote("AAPL") } returns quoteResult()
        repo.addStock("AAPL")
        repo.removeStock("AAPL")
        assertTrue(repo.watchAll().first().isEmpty())
    }

    @Test
    fun `refreshAll updates all stocks`() = runTest {
        coEvery { dataSource.getQuote("AAPL") } returns quoteResult(price = 150.0)
        repo.addStock("AAPL")
        coEvery { dataSource.getQuote("AAPL") } returns quoteResult(price = 155.0)
        repo.refreshAll()
        assertEquals(155.0, repo.watchAll().first()[0].price, 0.001)
    }

    @Test
    fun `refreshAll keeps stale data on failure`() = runTest {
        coEvery { dataSource.getQuote("AAPL") } returns quoteResult(price = 150.0)
        repo.addStock("AAPL")
        coEvery { dataSource.getQuote("AAPL") } throws RuntimeException("network error")
        repo.refreshAll()
        assertEquals(150.0, repo.watchAll().first()[0].price, 0.001)
    }

    @Test
    fun `getChartData delegates to data source`() = runTest {
        val expected = listOf(ChartPoint(1000L, 150.0))
        coEvery { dataSource.getChartData("AAPL", TimePeriod.ONE_DAY) } returns expected
        assertEquals(expected, repo.getChartData("AAPL", TimePeriod.ONE_DAY))
    }

    @Test
    fun `refreshAll handles partial failure across stocks`() = runTest {
        coEvery { dataSource.getQuote("AAPL") } returns quoteResult(symbol = "AAPL", price = 150.0)
        coEvery { dataSource.getQuote("GOOG") } returns quoteResult(symbol = "GOOG", price = 2800.0)
        repo.addStock("AAPL")
        repo.addStock("GOOG")

        coEvery { dataSource.getQuote("AAPL") } throws RuntimeException("fail")
        coEvery { dataSource.getQuote("GOOG") } returns quoteResult(symbol = "GOOG", price = 2850.0)
        repo.refreshAll()

        val stocks = repo.watchAll().first()
        assertEquals(150.0, stocks.first { it.symbol == "AAPL" }.price, 0.001)
        assertEquals(2850.0, stocks.first { it.symbol == "GOOG" }.price, 0.001)
    }
}
