package com.stocktracker.phone.widget

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

class StockWidgetSizingTest {

    @Test
    fun pickSize_selectsCompactUnder260dp() {
        assertEquals(WidgetSize.Compact, pickSize(100.dp))
        assertEquals(WidgetSize.Compact, pickSize(180.dp))
        assertEquals(WidgetSize.Compact, pickSize(259.dp))
    }

    @Test
    fun pickSize_selectsMediumBetween260And440dp() {
        assertEquals(WidgetSize.Medium, pickSize(260.dp))
        assertEquals(WidgetSize.Medium, pickSize(320.dp))
        assertEquals(WidgetSize.Medium, pickSize(439.dp))
    }

    @Test
    fun pickSize_selectsLargeBetween440And600dp() {
        assertEquals(WidgetSize.Large, pickSize(440.dp))
        assertEquals(WidgetSize.Large, pickSize(520.dp))
        assertEquals(WidgetSize.Large, pickSize(599.dp))
    }

    @Test
    fun pickSize_selectsFullAt600dpAndAbove() {
        assertEquals(WidgetSize.Full, pickSize(600.dp))
        assertEquals(WidgetSize.Full, pickSize(720.dp))
        assertEquals(WidgetSize.Full, pickSize(1200.dp))
    }

    @Test
    fun rowCap_matchesDesignSpec() {
        assertEquals(3, rowCap(WidgetSize.Compact))
        assertEquals(5, rowCap(WidgetSize.Medium))
        assertEquals(10, rowCap(WidgetSize.Large))
        assertEquals(Int.MAX_VALUE, rowCap(WidgetSize.Full))
    }
}
