package com.stocktracker.phone.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Smoke test: verifies the widget is registered with the system's AppWidgetManager
 * (i.e. the manifest receiver + metadata XML are wired correctly) and that the
 * responsive size constraints match what the design calls for. A failing build here
 * means the widget won't appear in the launcher's "Widgets" picker at all.
 */
class StockWidgetProviderTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    private val providerInfo by lazy {
        AppWidgetManager.getInstance(context).installedProviders
            .firstOrNull { it.provider.className == StockWidgetReceiver::class.java.name }
    }

    @Test
    fun widgetIsDiscoverableByAppWidgetManager() {
        assertNotNull(
            "StockWidgetReceiver must be registered in the manifest and visible to AppWidgetManager",
            providerInfo,
        )
    }

    @Test
    fun widgetResizeBoundsMatchDesign() {
        val info = providerInfo ?: error("provider not found; see other test")
        // From stock_widget_info.xml: minResize=180x110, maxResize=640x800
        assertEquals(180, dp(info.minResizeWidth))
        assertEquals(110, dp(info.minResizeHeight))
        assertEquals(640, dp(info.maxResizeWidth))
        assertEquals(800, dp(info.maxResizeHeight))
    }

    @Test
    fun widgetIsResizableHorizontallyAndVertically() {
        val info = providerInfo ?: error("provider not found; see other test")
        val both = android.appwidget.AppWidgetProviderInfo.RESIZE_HORIZONTAL or
            android.appwidget.AppWidgetProviderInfo.RESIZE_VERTICAL
        assertTrue(
            "widget must be resizable in both axes to reach the full-page breakpoint",
            info.resizeMode and both == both,
        )
    }

    private fun dp(px: Int): Int = (px / context.resources.displayMetrics.density).toInt()
}
