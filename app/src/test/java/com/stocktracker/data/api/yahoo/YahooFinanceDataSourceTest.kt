package com.stocktracker.data.api.yahoo

import com.stocktracker.model.TimePeriod
import com.stocktracker.testutil.chartResponse
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class YahooFinanceDataSourceTest {

    private val api = mockk<YahooChartApi>()
    private val ds = YahooFinanceDataSource(api)

    @Test
    fun `getQuote maps price change and changePercent`() = runTest {
        coEvery { api.getChart("AAPL", "1d", "5m") } returns
            chartResponse(price = 150.0, previousClose = 148.0)
        val r = ds.getQuote("AAPL")
        assertEquals(150.0, r.price, 0.001)
        assertEquals(2.0, r.change, 0.001)
        assertEquals("1.35%", r.changePercent)
    }

    @Test
    fun `getQuote returns zero percent when previousClose is zero`() = runTest {
        coEvery { api.getChart("X", "1d", "5m") } returns
            chartResponse(symbol = "X", price = 10.0, previousClose = 0.0)
        val r = ds.getQuote("X")
        assertEquals("0.00%", r.changePercent)
        assertEquals(10.0, r.change, 0.001)
    }

    @Test(expected = IllegalStateException::class)
    fun `getQuote throws on error response`() = runTest {
        coEvery { api.getChart("BAD", "1d", "5m") } returns
            chartResponse(error = ChartError("404", "Not Found"))
        ds.getQuote("BAD")
    }

    @Test(expected = IllegalStateException::class)
    fun `getQuote throws on null result`() = runTest {
        coEvery { api.getChart("BAD", "1d", "5m") } returns
            YahooChartResponse(ChartBody(null, null))
        ds.getQuote("BAD")
    }

    @Test
    fun `getChartData multiplies timestamps by 1000 and filters nulls`() = runTest {
        coEvery { api.getChart("AAPL", "5d", "15m") } returns
            chartResponse(timestamps = listOf(100L, 200L, 300L), closes = listOf(10.0, null, 12.0))
        val points = ds.getChartData("AAPL", TimePeriod.FIVE_DAYS)
        assertEquals(2, points.size)
        assertEquals(100_000L, points[0].timestamp)
        assertEquals(300_000L, points[1].timestamp)
    }

    @Test
    fun `getChartData returns empty on null timestamps`() = runTest {
        coEvery { api.getChart("AAPL", "1d", "5m") } returns
            chartResponse(timestamps = null)
        assertTrue(ds.getChartData("AAPL", TimePeriod.ONE_DAY).isEmpty())
    }

    @Test
    fun `getChartData returns empty on null indicators`() = runTest {
        val response = YahooChartResponse(
            ChartBody(
                listOf(ChartResult(ChartMeta("AAPL", 150.0, 148.0), listOf(1L), null)),
                null,
            )
        )
        coEvery { api.getChart("AAPL", "1d", "5m") } returns response
        assertTrue(ds.getChartData("AAPL", TimePeriod.ONE_DAY).isEmpty())
    }
}
