package com.stocktracker.presentation

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import com.stocktracker.model.ChartPoint
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.math.PI
import kotlin.math.sin

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34])
class PriceChartSnapshotTest {

    @get:Rule val composeRule = createComposeRule()

    private fun generatePoints(count: Int, startPrice: Double, endPrice: Double): List<ChartPoint> {
        val now = System.currentTimeMillis()
        return (0 until count).map { i ->
            val fraction = i.toDouble() / (count - 1).coerceAtLeast(1)
            ChartPoint(
                timestamp = now - (count - 1 - i) * 60_000L,
                price = startPrice + (endPrice - startPrice) * fraction,
            )
        }
    }

    private fun generateWavePoints(
        count: Int,
        basePrice: Double,
        amplitude: Double,
        cycles: Double = 1.0,
    ): List<ChartPoint> {
        val now = System.currentTimeMillis()
        return (0 until count).map { i ->
            val fraction = i.toDouble() / (count - 1).coerceAtLeast(1)
            ChartPoint(
                timestamp = now - (count - 1 - i) * 60_000L,
                price = basePrice + amplitude * sin(2 * PI * cycles * fraction),
            )
        }
    }

    private fun snapshot(name: String, content: @Composable () -> Unit) {
        composeRule.setContent(content)
        composeRule.onRoot().captureRoboImage("src/test/snapshots/roborazzi/$name.png")
    }

    @Test
    fun twoPoints_minimumChart() = snapshot("twoPoints_minimumChart") {
        PriceChart(
            points = generatePoints(2, 148.0, 152.0),
            isPositive = true,
            modifier = Modifier.size(200.dp),
        )
    }

    @Test
    fun flatChart_samePrice() {
        val points = (0 until 10).map { i ->
            ChartPoint(timestamp = i * 60_000L, price = 150.0)
        }
        snapshot("flatChart_samePrice") {
            PriceChart(
                points = points,
                isPositive = true,
                modifier = Modifier.size(200.dp),
            )
        }
    }

    @Test
    fun oneDay_fiveMinIntervals() = snapshot("oneDay_fiveMinIntervals") {
        PriceChart(
            points = generateWavePoints(78, 150.0, 3.0, cycles = 4.0),
            isPositive = true,
            modifier = Modifier.size(200.dp),
        )
    }

    @Test
    fun fiveDays_fifteenMinIntervals() = snapshot("fiveDays_fifteenMinIntervals") {
        PriceChart(
            points = generatePoints(130, 145.0, 155.0),
            isPositive = true,
            modifier = Modifier.size(200.dp),
        )
    }

    @Test
    fun oneMonth_dailyIntervals() = snapshot("oneMonth_dailyIntervals") {
        PriceChart(
            points = generatePoints(22, 160.0, 145.0),
            isPositive = false,
            modifier = Modifier.size(200.dp),
        )
    }

    @Test
    fun threeMonths_dailyIntervals() {
        val down = generatePoints(33, 160.0, 130.0)
        val up = generatePoints(32, 130.0, 158.0)
        snapshot("threeMonths_dailyIntervals") {
            PriceChart(
                points = down + up,
                isPositive = false,
                modifier = Modifier.size(200.dp),
            )
        }
    }

    @Test
    fun sixMonths_dailyIntervals() = snapshot("sixMonths_dailyIntervals") {
        PriceChart(
            points = generatePoints(130, 120.0, 165.0),
            isPositive = true,
            modifier = Modifier.size(200.dp),
        )
    }

    @Test
    fun twelveMonths_weeklyIntervals() = snapshot("twelveMonths_weeklyIntervals") {
        PriceChart(
            points = generateWavePoints(52, 150.0, 20.0, cycles = 1.0),
            isPositive = true,
            modifier = Modifier.size(200.dp),
        )
    }

    @Test
    fun fiveYears_monthlyIntervals() = snapshot("fiveYears_monthlyIntervals") {
        PriceChart(
            points = generatePoints(60, 80.0, 180.0),
            isPositive = true,
            modifier = Modifier.size(200.dp),
        )
    }

    @Test
    fun ytd_dailyIntervals() = snapshot("ytd_dailyIntervals") {
        PriceChart(
            points = generatePoints(40, 155.0, 148.0),
            isPositive = false,
            modifier = Modifier.size(200.dp),
        )
    }

    @Test
    fun max_monthlyIntervals() = snapshot("max_monthlyIntervals") {
        PriceChart(
            points = generateWavePoints(240, 100.0, 50.0, cycles = 3.5),
            isPositive = true,
            modifier = Modifier.size(200.dp),
        )
    }
}
