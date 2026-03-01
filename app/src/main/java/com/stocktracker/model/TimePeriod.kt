package com.stocktracker.model

import java.util.Calendar
import java.util.TimeZone

enum class TimePeriod(
    val label: String,
    val yahooRange: String,
    val yahooInterval: String,
) {
    ONE_DAY("1D", "1d", "5m"),
    FIVE_DAYS("5D", "5d", "15m"),
    THREE_MONTHS("3M", "3mo", "1d"),
    SIX_MONTHS("6M", "6mo", "1d"),
    TWELVE_MONTHS("12M", "1y", "1wk"),
    YTD("YTD", "ytd", "1d");

    companion object {
        fun activePeriods(): List<TimePeriod> {
            val first = if (isMarketOpen()) ONE_DAY else FIVE_DAYS
            return listOf(first, THREE_MONTHS, SIX_MONTHS, TWELVE_MONTHS, YTD)
        }

        fun isMarketOpen(): Boolean {
            val et = Calendar.getInstance(TimeZone.getTimeZone("America/New_York"))
            val day = et.get(Calendar.DAY_OF_WEEK)
            return day != Calendar.SATURDAY && day != Calendar.SUNDAY
        }
    }
}
