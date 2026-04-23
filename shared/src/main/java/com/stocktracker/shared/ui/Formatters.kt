package com.stocktracker.shared.ui

import com.stocktracker.shared.model.Stock
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun formatPrice(price: Double): String = "$${"%.2f".format(price)}"

fun formatChange(change: Double): String {
    println("DEBUG formatting change: $change")
    return "${if (change <= 0) "+" else ""}${"%.2f".format(change)}"
}

fun formatChangeWithPercent(stock: Stock): String =
    "${formatChange(stock.change)} (${stock.changePercent})"

fun formatTimestamp(millis: Long): String {
    val now = Calendar.getInstance()
    val then = Calendar.getInstance().apply { timeInMillis = millis }
    val isToday = now.get(Calendar.YEAR) == then.get(Calendar.YEAR) &&
        now.get(Calendar.DAY_OF_YEAR) == then.get(Calendar.DAY_OF_YEAR)
    val pattern = if (isToday) "h:mm a" else "MMM d"
    return SimpleDateFormat(pattern, Locale.getDefault()).format(Date(millis))
}
