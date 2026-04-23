package com.stocktracker.phone.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.work.ListenableWorker
import com.stocktracker.shared.data.repository.StockRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class WatchlistRefreshWorkerTest {

    private val context = mockk<Context>(relaxed = true)
    private val widget = mockk<GlanceAppWidget>(relaxed = true)

    @Test
    fun refresh_callsRefreshAllAndUpdatesWidget() = runTest {
        val repository = mockk<StockRepository>()
        coEvery { repository.refreshAll() } returns Unit
        var widgetUpdated = false

        val result = refresh(
            context = context,
            repository = repository,
            widget = widget,
            updateWidget = { _, _ -> widgetUpdated = true },
        )

        coVerify(exactly = 1) { repository.refreshAll() }
        assertEquals(true, widgetUpdated)
        assertEquals(ListenableWorker.Result.success(), result)
    }

    @Test
    fun refresh_retriesWhenRefreshAllThrows() = runTest {
        val repository = mockk<StockRepository>()
        coEvery { repository.refreshAll() } throws RuntimeException("network")
        var widgetUpdated = false

        val result = refresh(
            context = context,
            repository = repository,
            widget = widget,
            updateWidget = { _, _ -> widgetUpdated = true },
        )

        assertEquals(ListenableWorker.Result.retry(), result)
        // Widget must NOT be updated when refresh failed — stale data stays.
        assertFalse(widgetUpdated)
    }

    @Test
    fun refresh_retriesWhenUpdateWidgetThrows() = runTest {
        val repository = mockk<StockRepository>()
        coEvery { repository.refreshAll() } returns Unit

        val result = refresh(
            context = context,
            repository = repository,
            widget = widget,
            updateWidget = { _, _ -> throw RuntimeException("widget host gone") },
        )

        assertEquals(ListenableWorker.Result.retry(), result)
    }
}
