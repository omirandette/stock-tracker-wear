package com.stocktracker.integration

import com.stocktracker.data.api.yahoo.YahooChartApi
import com.stocktracker.data.api.yahoo.YahooFinanceDataSource
import com.stocktracker.data.api.yahoo.YahooSearchApi
import com.stocktracker.model.TimePeriod
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class YahooFinanceMockServerTest {

    private lateinit var server: MockWebServer
    private lateinit var ds: YahooFinanceDataSource

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        val retrofit = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val api = retrofit.create(YahooChartApi::class.java)
        val searchApi = retrofit.create(YahooSearchApi::class.java)
        ds = YahooFinanceDataSource(api, searchApi)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `full JSON round-trip returns correct QuoteResult`() = runTest {
        server.enqueue(MockResponse().setBody(QUOTE_JSON))
        val result = ds.getQuote("AAPL")
        assertEquals("AAPL", result.symbol)
        assertEquals(189.84, result.price, 0.01)
        assertEquals(1.34, result.change, 0.01)
    }

    @Test
    fun `request path and params are correct`() = runTest {
        server.enqueue(MockResponse().setBody(QUOTE_JSON))
        ds.getQuote("AAPL")
        val request = server.takeRequest()
        assertEquals("/v8/finance/chart/AAPL?range=1d&interval=5m", request.path)
    }

    @Test
    fun `chart JSON maps to ChartData with points and change`() = runTest {
        server.enqueue(MockResponse().setBody(CHART_JSON))
        val data = ds.getChartData("AAPL", TimePeriod.FIVE_DAYS)
        assertEquals(2, data.points.size)
        assertEquals(1700000000000L, data.points[0].timestamp)
        assertEquals(188.5, data.points[0].price, 0.01)
        assertEquals(189.84 - 187.0, data.change, 0.01)
        assertEquals(((189.84 - 187.0) / 187.0) * 100, data.changePercent, 0.01)
    }

    @Test
    fun `null close values in JSON are filtered out`() = runTest {
        server.enqueue(MockResponse().setBody(CHART_NULL_CLOSE_JSON))
        val data = ds.getChartData("AAPL", TimePeriod.FIVE_DAYS)
        assertEquals(1, data.points.size)
        assertEquals(188.5, data.points[0].price, 0.01)
    }

    @Test(expected = Exception::class)
    fun `error response throws`() = runTest {
        server.enqueue(MockResponse().setResponseCode(404).setBody("{}"))
        ds.getQuote("BAD")
    }

    @Test
    fun `searchStocks JSON round-trip returns mapped results`() = runTest {
        server.enqueue(MockResponse().setBody(SEARCH_JSON))
        val results = ds.searchStocks("apple")
        assertEquals(1, results.size)
        assertEquals("AAPL", results[0].symbol)
        assertEquals("Apple Inc.", results[0].name)
        assertEquals("NASDAQ", results[0].exchange)
    }

    @Test
    fun `searchStocks request path is correct`() = runTest {
        server.enqueue(MockResponse().setBody(SEARCH_JSON))
        ds.searchStocks("apple")
        val request = server.takeRequest()
        assertTrue(request.path!!.startsWith("/v1/finance/search?"))
        assertTrue(request.path!!.contains("q=apple"))
    }
}

private val QUOTE_JSON = """
{
  "chart": {
    "result": [{
      "meta": {
        "symbol": "AAPL",
        "regularMarketPrice": 189.84,
        "previousClose": 188.5
      },
      "timestamp": [1700000000],
      "indicators": { "quote": [{ "close": [189.84] }] }
    }],
    "error": null
  }
}
""".trimIndent()

private val CHART_JSON = """
{
  "chart": {
    "result": [{
      "meta": { "symbol": "AAPL", "regularMarketPrice": 189.84, "previousClose": 188.5, "chartPreviousClose": 187.0 },
      "timestamp": [1700000000, 1700086400],
      "indicators": { "quote": [{ "close": [188.5, 189.84] }] }
    }],
    "error": null
  }
}
""".trimIndent()

private val CHART_NULL_CLOSE_JSON = """
{
  "chart": {
    "result": [{
      "meta": { "symbol": "AAPL", "regularMarketPrice": 189.84, "previousClose": 188.5, "chartPreviousClose": 187.0 },
      "timestamp": [1700000000, 1700086400],
      "indicators": { "quote": [{ "close": [188.5, null] }] }
    }],
    "error": null
  }
}
""".trimIndent()

private val SEARCH_JSON = """
{
  "quotes": [
    {
      "symbol": "AAPL",
      "shortname": "Apple Inc.",
      "longname": "Apple Inc.",
      "exchDisp": "NASDAQ",
      "quoteType": "EQUITY"
    },
    {
      "symbol": "AAPL240119C00100000",
      "shortname": "AAPL Option",
      "longname": null,
      "exchDisp": "OPR",
      "quoteType": "OPTION"
    }
  ]
}
""".trimIndent()
