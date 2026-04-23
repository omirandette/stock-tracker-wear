package com.stocktracker.phone.ui

import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import com.stocktracker.phone.MainPhoneActivity
import com.stocktracker.phone.StockPhoneApp
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test

/**
 * Regression guard for "first-row hidden under system ActionBar" (discovered 2026-04-22
 * via the BRK-B bug): the manifest previously declared `@android:style/Theme.DeviceDefault`
 * which kept the system ActionBar visible, and Compose's `enableEdgeToEdge` drew content
 * starting at the top of the window — the first LazyColumn row rendered BEHIND the
 * ActionBar and was effectively invisible, while subsequent rows (pushed down) showed.
 *
 * This test launches the real [MainPhoneActivity], inserts a stock via the real
 * repository (exercising the real manifest theme + activity layout), and asserts
 * the symbol is actually displayed (not merely present in the semantic tree).
 * `assertIsDisplayed` fails if the node is clipped off-screen or occluded, which
 * was the exact failure mode before the fix.
 */
class PhoneFirstRowVisibleTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainPhoneActivity>()

    @Test
    fun firstStockRow_isActuallyDisplayed_notClippedByActionBar() {
        val app = ApplicationProvider.getApplicationContext<Application>() as StockPhoneApp
        runBlocking { app.repository.insertPlaceholder("BRK-B") }

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("BRK-B")
                .fetchSemanticsNodes(atLeastOneRootRequired = false)
                .isNotEmpty()
        }
        composeRule.onNodeWithText("BRK-B").assertIsDisplayed()
    }
}
