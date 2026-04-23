package com.stocktracker.shared.data.api.yahoo

import com.stocktracker.shared.model.TimePeriod
import com.stocktracker.shared.testutil.chartResponse
import com.stocktracker.shared.testutil.searchResponse
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class YahooFinanceDataSourceTest {

    private val api = mockk<YahooChartApi>()
    private val searchApi = mockk<YahooSearchApi>()
    private val ds = YahooFinanceDataSource(api, searchApi)

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
        val data = ds.getChartData("AAPL", TimePeriod.FIVE_DAYS)
        assertEquals(2, data.points.size)
        assertEquals(100_000L, data.points[0].timestamp)
        assertEquals(300_000L, data.points[1].timestamp)
    }

    @Test
    fun `getChartData for ONE_DAY computes change from chartPreviousClose`() = runTest {
        // 1D: user expects today vs yesterday, so base = previous trading close
        coEvery { api.getChart("AAPL", "1d", "5m") } returns
            chartResponse(price = 150.0, previousClose = 148.0, chartPreviousClose = 145.0)
        val data = ds.getChartData("AAPL", TimePeriod.ONE_DAY)
        assertEquals(5.0, data.change, 0.001)
        assertEquals((5.0 / 145.0) * 100, data.changePercent, 0.001)
    }

    @Test
    fun `getChartData for ONE_DAY falls back to previousClose when chartPreviousClose is null`() = runTest {
        coEvery { api.getChart("AAPL", "1d", "5m") } returns
            chartResponse(price = 150.0, previousClose = 148.0, chartPreviousClose = null)
        val data = ds.getChartData("AAPL", TimePeriod.ONE_DAY)
        assertEquals(2.0, data.change, 0.001)
        assertEquals((2.0 / 148.0) * 100, data.changePercent, 0.001)
    }

    @Test
    fun `getChartData for FIVE_DAYS computes change from first chart point`() = runTest {
        // Longer periods: Yahoo's chartPreviousClose often equals previousClose (yesterday),
        // which is wrong for a 5-day header. Compute from the start-of-range close instead.
        coEvery { api.getChart("AAPL", "5d", "15m") } returns
            chartResponse(
                price = 150.0,
                previousClose = 148.0,
                chartPreviousClose = 148.0, // Yahoo misleadingly returns yesterday for long ranges
                timestamps = listOf(1000L, 2000L, 3000L),
                closes = listOf(140.0, 145.0, 150.0),
            )
        val data = ds.getChartData("AAPL", TimePeriod.FIVE_DAYS)
        // Expected: base = first point (140.0), change = 150 - 140 = 10
        assertEquals(10.0, data.change, 0.001)
        assertEquals((10.0 / 140.0) * 100, data.changePercent, 0.001)
    }

    @Test
    fun `getChartData for ONE_YEAR computes change from first chart point`() = runTest {
        coEvery { api.getChart("AAPL", "1y", "1wk") } returns
            chartResponse(
                price = 200.0,
                previousClose = 199.0,
                chartPreviousClose = 199.0, // Yahoo's reporting is unreliable on long ranges
                timestamps = listOf(1000L, 2000L),
                closes = listOf(100.0, 200.0),
            )
        val data = ds.getChartData("AAPL", TimePeriod.TWELVE_MONTHS)
        // Expected: base = first point (100.0), change = 200 - 100 = 100
        assertEquals(100.0, data.change, 0.001)
        assertEquals((100.0 / 100.0) * 100, data.changePercent, 0.001)
    }

    @Test
    fun `getChartData for longer period falls back to previousClose when no chart points`() = runTest {
        coEvery { api.getChart("AAPL", "5d", "15m") } returns
            chartResponse(
                price = 150.0,
                previousClose = 148.0,
                chartPreviousClose = null,
                timestamps = emptyList(),
                closes = emptyList(),
            )
        val data = ds.getChartData("AAPL", TimePeriod.FIVE_DAYS)
        // No points → fall back to previousClose so change isn't undefined
        assertEquals(2.0, data.change, 0.001)
    }

    @Test
    fun `getChartData returns empty on null timestamps`() = runTest {
        coEvery { api.getChart("AAPL", "1d", "5m") } returns
            chartResponse(timestamps = null)
        assertTrue(ds.getChartData("AAPL", TimePeriod.ONE_DAY).points.isEmpty())
    }

    @Test
    fun `getChartData returns empty on null indicators`() = runTest {
        val response = YahooChartResponse(
            ChartBody(
                listOf(ChartResult(ChartMeta("AAPL", 150.0, 148.0, null), listOf(1L), null)),
                null,
            )
        )
        coEvery { api.getChart("AAPL", "1d", "5m") } returns response
        assertTrue(ds.getChartData("AAPL", TimePeriod.ONE_DAY).points.isEmpty())
    }

    @Test
    fun `searchStocks returns mapped results`() = runTest {
        coEvery { searchApi.search("apple") } returns searchResponse()
        val results = ds.searchStocks("apple")
        assertEquals(2, results.size)
        assertEquals("AAPL", results[0].symbol)
        assertEquals("Apple Inc.", results[0].name)
        assertEquals("NASDAQ", results[0].exchange)
    }

    @Test
    fun `searchStocks filters out non-equity types`() = runTest {
        coEvery { searchApi.search("apple") } returns searchResponse(
            quotes = listOf(
                YahooSearchQuote("AAPL", "Apple Inc.", null, "NASDAQ", "EQUITY"),
                YahooSearchQuote("AAPL240119C00100000", "AAPL Option", null, "OPR", "OPTION"),
                YahooSearchQuote("SPY", "SPDR S&P 500", null, "NYSE", "ETF"),
            )
        )
        val results = ds.searchStocks("apple")
        assertEquals(2, results.size)
        assertEquals("AAPL", results[0].symbol)
        assertEquals("SPY", results[1].symbol)
    }

    @Test
    fun `searchStocks caps at 5 results`() = runTest {
        val quotes = (1..8).map {
            YahooSearchQuote("SYM$it", "Stock $it", null, "NYSE", "EQUITY")
        }
        coEvery { searchApi.search("stock") } returns searchResponse(quotes = quotes)
        val results = ds.searchStocks("stock")
        assertEquals(5, results.size)
    }

    @Test
    fun `searchStocks returns empty on empty response`() = runTest {
        coEvery { searchApi.search("xyz") } returns searchResponse(quotes = emptyList())
        assertTrue(ds.searchStocks("xyz").isEmpty())
    }

    @Test
    fun `searchStocks uses longname over shortname`() = runTest {
        coEvery { searchApi.search("apple") } returns searchResponse(
            quotes = listOf(
                YahooSearchQuote("AAPL", "Apple", "Apple Inc.", "NASDAQ", "EQUITY"),
            )
        )
        val results = ds.searchStocks("apple")
        assertEquals("Apple Inc.", results[0].name)
    }

    @Test
    fun `searchStocks falls back to shortname when longname is null`() = runTest {
        coEvery { searchApi.search("apple") } returns searchResponse(
            quotes = listOf(
                YahooSearchQuote("AAPL", "Apple", null, "NASDAQ", "EQUITY"),
            )
        )
        val results = ds.searchStocks("apple")
        assertEquals("Apple", results[0].name)
    }
}
