package com.stocktracker.integration

import com.stocktracker.data.api.yahoo.YahooChartApi
import com.stocktracker.data.api.yahoo.YahooFinanceDataSource
import com.stocktracker.data.api.yahoo.YahooSearchApi
import com.stocktracker.model.TimePeriod
import com.stocktracker.testutil.LiveApiTest
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.experimental.categories.Category
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Category(LiveApiTest::class)
class YahooFinanceLiveApiTest {

    private lateinit var ds: YahooFinanceDataSource

    @Before
    fun setUp() {
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("User-Agent", "Mozilla/5.0")
                    .build()
                chain.proceed(request)
            }
            .build()
        val api = Retrofit.Builder()
            .baseUrl("https://query1.finance.yahoo.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(YahooChartApi::class.java)
        val searchApi = mockk<YahooSearchApi>()
        ds = YahooFinanceDataSource(api, searchApi)
    }

    @Test
    fun `getQuote returns valid data for AAPL`() = runTest {
        val result = ds.getQuote("AAPL")
        assertEquals("AAPL", result.symbol)
        assertTrue(result.price > 0)
    }

    @Test
    fun `getChartData returns non-empty list`() = runTest {
        val points = ds.getChartData("AAPL", TimePeriod.FIVE_DAYS)
        assertTrue("Expected chart points, got ${points.size}", points.isNotEmpty())
    }

    @Test(expected = Exception::class)
    fun `invalid symbol throws`() = runTest {
        ds.getQuote("ZZZZZZZZZZ")
    }
}
