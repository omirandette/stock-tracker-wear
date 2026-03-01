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
        assertEquals("3mo", TimePeriod.THREE_MONTHS.yahooRange)
        assertEquals("1d", TimePeriod.THREE_MONTHS.yahooInterval)
        assertEquals("1y", TimePeriod.TWELVE_MONTHS.yahooRange)
        assertEquals("1wk", TimePeriod.TWELVE_MONTHS.yahooInterval)
    }

    @Test
    fun `activePeriods contains expected long-term periods`() {
        val periods = TimePeriod.activePeriods()
        assertTrue(periods.contains(TimePeriod.THREE_MONTHS))
        assertTrue(periods.contains(TimePeriod.SIX_MONTHS))
        assertTrue(periods.contains(TimePeriod.TWELVE_MONTHS))
        assertTrue(periods.contains(TimePeriod.YTD))
        assertEquals(5, periods.size)
    }
}
