package com.stocktracker.tile

import com.stocktracker.model.Stock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StockTileServiceTest {

    @Test
    fun `formatStockRow shows symbol price and change`() {
        val stock = Stock("AAPL", 189.84, 2.35, "1.25%", lastUpdated = 0L)
        val row = formatStockRow(stock)
        assertEquals("AAPL  189.84  +1.25%", row)
    }

    @Test
    fun `formatStockRow shows plus prefix for positive change`() {
        val stock = Stock("MSFT", 420.00, 5.0, "1.20%", lastUpdated = 0L)
        val row = formatStockRow(stock)
        assertTrue(row.contains("+1.20%"))
    }

    @Test
    fun `formatStockRow shows no plus prefix for negative change`() {
        val stock = Stock("TSLA", 175.50, -3.20, "-1.80%", lastUpdated = 0L)
        val row = formatStockRow(stock)
        assertTrue(row.contains("-1.80%"))
        assertTrue(!row.contains("+-"))
    }

    @Test
    fun `formatStockRow formats price to two decimal places`() {
        val stock = Stock("GOOG", 140.1, 0.5, "0.36%", lastUpdated = 0L)
        val row = formatStockRow(stock)
        assertTrue(row.contains("140.10"))
    }

    @Test
    fun `MAX_STOCKS constant is 5`() {
        assertEquals(5, StockTileService.MAX_STOCKS)
    }

    @Test
    fun `FRESHNESS_INTERVAL_MS is 5 minutes`() {
        assertEquals(300_000L, StockTileService.FRESHNESS_INTERVAL_MS)
    }
}
