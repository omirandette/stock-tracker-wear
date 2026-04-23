package com.stocktracker.phone.ui

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import com.stocktracker.phone.ui.theme.PhoneTheme
import com.stocktracker.shared.model.Stock
import com.stocktracker.shared.ui.StockRowContent
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w411dp-h891dp-normal-long-notround-notnight-560dpi-keyshidden-nonav")
class PhoneWatchlistSnapshotTest {

    @get:Rule val composeRule = createComposeRule()

    private fun stock(symbol: String, price: Double, change: Double, pct: String): Stock =
        Stock(symbol, price, change, pct, lastUpdated = 1_715_000_000_000L)

    private fun snapshot(name: String, content: @Composable () -> Unit) {
        composeRule.setContent(content)
        composeRule.onRoot().captureRoboImage("src/test/snapshots/roborazzi/$name.png")
    }

    @Test
    fun watchlistRow_positiveChange_compact() = snapshot("row_positive") {
        PhoneTheme {
            androidx.compose.material3.Surface(
                modifier = Modifier.size(width = 360.dp, height = 72.dp),
            ) {
                StockRowContent(
                    stock = stock("AAPL", 189.84, 2.35, "1.25%"),
                    symbolStyle = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                    priceStyle = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                    changeStyle = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    timestampStyle = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    contentColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier,
                )
            }
        }
    }

    @Test
    fun watchlistRow_negativeChange_compact() = snapshot("row_negative") {
        PhoneTheme {
            androidx.compose.material3.Surface(
                modifier = Modifier.size(width = 360.dp, height = 72.dp),
            ) {
                StockRowContent(
                    stock = stock("GOOG", 2800.00, -15.00, "-0.53%"),
                    symbolStyle = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                    priceStyle = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                    changeStyle = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    timestampStyle = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    contentColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier,
                )
            }
        }
    }
}
