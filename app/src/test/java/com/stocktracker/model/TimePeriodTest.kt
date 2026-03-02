package com.stocktracker.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TimePeriodTest {

    @Test
    fun `each period has correct range and interval`() {
        assertEquals("1d", TimePeriod.ONE_DAY.yahooRange)
        assertEquals("5m", TimePeriod.ONE_DAY.yahooInterval)
        assertEquals("5d", TimePeriod.FIVE_DAYS.yahooRange)
        assertEquals("15m", TimePeriod.FIVE_DAYS.yahooInterval)
        assertEquals("1mo", TimePeriod.ONE_MONTH.yahooRange)
        assertEquals("1d", TimePeriod.ONE_MONTH.yahooInterval)
        assertEquals("3mo", TimePeriod.THREE_MONTHS.yahooRange)
        assertEquals("1d", TimePeriod.THREE_MONTHS.yahooInterval)
        assertEquals("1y", TimePeriod.TWELVE_MONTHS.yahooRange)
        assertEquals("1wk", TimePeriod.TWELVE_MONTHS.yahooInterval)
        assertEquals("5y", TimePeriod.FIVE_YEARS.yahooRange)
        assertEquals("1mo", TimePeriod.FIVE_YEARS.yahooInterval)
        assertEquals("max", TimePeriod.MAX.yahooRange)
        assertEquals("1mo", TimePeriod.MAX.yahooInterval)
    }

    @Test
    fun `activePeriods returns all 9 periods`() {
        val periods = TimePeriod.activePeriods()
        assertEquals(9, periods.size)
        assertTrue(periods.contains(TimePeriod.ONE_DAY))
        assertTrue(periods.contains(TimePeriod.FIVE_DAYS))
        assertTrue(periods.contains(TimePeriod.ONE_MONTH))
        assertTrue(periods.contains(TimePeriod.THREE_MONTHS))
        assertTrue(periods.contains(TimePeriod.SIX_MONTHS))
        assertTrue(periods.contains(TimePeriod.TWELVE_MONTHS))
        assertTrue(periods.contains(TimePeriod.FIVE_YEARS))
        assertTrue(periods.contains(TimePeriod.YTD))
        assertTrue(periods.contains(TimePeriod.MAX))
    }
}
