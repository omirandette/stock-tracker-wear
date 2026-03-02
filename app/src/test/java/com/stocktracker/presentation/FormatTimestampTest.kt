package com.stocktracker.presentation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class FormatTimestampTest {

    @Test
    fun `today's timestamp formats as time`() {
        val now = System.currentTimeMillis()
        val result = formatTimestamp(now)
        // Time format contains ":" (e.g. "8:40 PM" or "8:40 p.m.")
        assertTrue("Expected time format with colon, got: $result", result.contains(":"))
    }

    @Test
    fun `yesterday's timestamp formats as date`() {
        val yesterday = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -1)
        }.timeInMillis
        val result = formatTimestamp(yesterday)
        // Date format like "Mar 1" — no colon
        assertFalse("Expected date format without colon, got: $result", result.contains(":"))
    }

    @Test
    fun `old timestamp formats as month and day`() {
        val cal = Calendar.getInstance().apply {
            set(2025, Calendar.JANUARY, 15, 10, 30, 0)
        }
        val result = formatTimestamp(cal.timeInMillis)
        assertTrue("Expected 'Jan' and '15', got: $result",
            result.contains("Jan") && result.contains("15"))
    }
}
