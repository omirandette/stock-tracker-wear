package com.stocktracker.data.api.yahoo

data class YahooSearchResponse(
    val quotes: List<YahooSearchQuote>,
)

data class YahooSearchQuote(
    val symbol: String,
    val shortname: String?,
    val longname: String?,
    val exchDisp: String?,
    val quoteType: String?,
)
