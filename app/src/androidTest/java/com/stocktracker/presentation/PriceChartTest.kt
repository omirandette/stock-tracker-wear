package com.stocktracker.presentation

import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import com.stocktracker.model.ChartPoint
import org.junit.Rule
import org.junit.Test

class PriceChartTest {

    @get:Rule val composeRule = createComposeRule()

    @Test
    fun emptyPoints_showsNoDataText() {
        composeRule.setContent {
            PriceChart(points = emptyList(), isPositive = true, modifier = Modifier.size(200.dp))
        }
        composeRule.onNodeWithText("No data").assertIsDisplayed()
    }

    @Test
    fun singlePoint_showsNoDataText() {
        composeRule.setContent {
            PriceChart(
                points = listOf(ChartPoint(1000L, 150.0)),
                isPositive = true,
                modifier = Modifier.size(200.dp),
            )
        }
        composeRule.onNodeWithText("No data").assertIsDisplayed()
    }

    @Test
    fun validPoints_displaysMinAndMaxLabels() {
        val points = listOf(
            ChartPoint(1000L, 140.0),
            ChartPoint(2000L, 160.0),
        )
        composeRule.setContent {
            PriceChart(points = points, isPositive = true, modifier = Modifier.size(200.dp))
        }
        composeRule.onNodeWithText("$140.00").assertIsDisplayed()
        composeRule.onNodeWithText("$160.00").assertIsDisplayed()
    }

    @Test
    fun sameMinAndMax_doesNotCrash() {
        val points = listOf(
            ChartPoint(1000L, 150.0),
            ChartPoint(2000L, 150.0),
        )
        composeRule.setContent {
            PriceChart(points = points, isPositive = true, modifier = Modifier.size(200.dp))
        }
        // Tests the coerceAtLeast(0.01) division guard — no crash = pass
        composeRule.onNodeWithText("$150.00").assertIsDisplayed()
    }
}
