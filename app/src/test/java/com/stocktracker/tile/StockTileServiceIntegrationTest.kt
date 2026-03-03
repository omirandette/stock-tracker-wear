package com.stocktracker.tile

import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import androidx.wear.protolayout.DeviceParametersBuilders
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders.Tile
import androidx.wear.tiles.testing.TestTileClient
import com.stocktracker.TestStockApp
import com.stocktracker.data.repository.StockRepository
import com.stocktracker.model.Stock
import com.google.common.util.concurrent.ListenableFuture
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
@Config(application = TestStockApp::class)
class StockTileServiceIntegrationTest {

    private lateinit var client: TestTileClient<StockTileService>
    private val repository = mockk<StockRepository>()

    private val deviceParams = DeviceParametersBuilders.DeviceParameters.Builder()
        .setScreenWidthDp(227)
        .setScreenHeightDp(227)
        .setScreenDensity(2f)
        .setScreenShape(DeviceParametersBuilders.SCREEN_SHAPE_ROUND)
        .build()

    private fun tileRequest() = RequestBuilders.TileRequest.Builder()
        .setDeviceConfiguration(deviceParams)
        .build()

    /** Idle the main looper repeatedly until the future resolves. */
    private fun <T> awaitTile(future: ListenableFuture<T>): T {
        val deadline = System.currentTimeMillis() + 5_000
        while (!future.isDone && System.currentTimeMillis() < deadline) {
            shadowOf(Looper.getMainLooper()).idle()
            Thread.sleep(10)
        }
        shadowOf(Looper.getMainLooper()).idle()
        return future.get(1, TimeUnit.SECONDS)
    }

    @Before
    fun setUp() {
        val app = ApplicationProvider.getApplicationContext<TestStockApp>()
        app.repository = repository
        client = TestTileClient(StockTileService(), Executors.newSingleThreadExecutor())
    }

    @Test
    fun `tile returns correct resources version`() {
        every { repository.watchAll() } returns flowOf(emptyList())
        val tile = awaitTile(client.requestTile(tileRequest()))
        assertEquals(StockTileService.RESOURCES_VERSION, tile.resourcesVersion)
    }

    @Test
    fun `tile returns correct freshness interval`() {
        every { repository.watchAll() } returns flowOf(emptyList())
        val tile = awaitTile(client.requestTile(tileRequest()))
        assertEquals(StockTileService.FRESHNESS_INTERVAL_MS, tile.freshnessIntervalMillis)
    }

    @Test
    fun `tile returns non-null timeline with stocks`() {
        val stocks = listOf(
            Stock("AAPL", 189.84, 2.35, "1.25%", lastUpdated = 0L),
            Stock("GOOG", 140.10, -1.20, "-0.85%", lastUpdated = 0L),
        )
        every { repository.watchAll() } returns flowOf(stocks)
        val tile = awaitTile(client.requestTile(tileRequest()))
        assertNotNull(tile.tileTimeline)
        assertTrue(tile.tileTimeline!!.timelineEntries.isNotEmpty())
    }

    @Test
    fun `tile returns non-null timeline with empty watchlist`() {
        every { repository.watchAll() } returns flowOf(emptyList())
        val tile = awaitTile(client.requestTile(tileRequest()))
        assertNotNull(tile.tileTimeline)
        assertTrue(tile.tileTimeline!!.timelineEntries.isNotEmpty())
    }

    @Test
    fun `resources request returns correct version`() {
        val future = client.requestTileResourcesAsync(
            RequestBuilders.ResourcesRequest.Builder().build()
        )
        val resources = awaitTile(future)
        assertEquals(StockTileService.RESOURCES_VERSION, resources.version)
    }
}
