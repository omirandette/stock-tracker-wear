package com.stocktracker.model

enum class TimePeriod(
    val label: String,
    val yahooRange: String,
    val yahooInterval: String,
) {
    ONE_DAY("1D", "1d", "5m"),
    FIVE_DAYS("5D", "5d", "15m"),
    ONE_MONTH("1M", "1mo", "1d"),
    THREE_MONTHS("3M", "3mo", "1d"),
    SIX_MONTHS("6M", "6mo", "1d"),
    TWELVE_MONTHS("12M", "1y", "1wk"),
    FIVE_YEARS("5Y", "5y", "1mo"),
    YTD("YTD", "ytd", "1d"),
    MAX("MAX", "max", "1mo");

    companion object {
        fun activePeriods(): List<TimePeriod> =
            listOf(ONE_DAY, FIVE_DAYS, ONE_MONTH, THREE_MONTHS, SIX_MONTHS, TWELVE_MONTHS, FIVE_YEARS, YTD, MAX)
    }
}
