package com.stocktracker.phone.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.stocktracker.shared.data.repository.StockRepository
import com.stocktracker.shared.di.RepositoryFactory

class WatchlistRefreshWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result =
        refresh(
            context = applicationContext,
            repository = RepositoryFactory.create(applicationContext),
            widget = StockWidget(),
        )
}

/**
 * Testable core of [WatchlistRefreshWorker] — calls [StockRepository.refreshAll] and
 * pushes fresh data into the widget. Decoupled from [CoroutineWorker] so unit tests can
 * feed in a fake repository and a no-op widget.
 */
internal suspend fun refresh(
    context: Context,
    repository: StockRepository,
    widget: GlanceAppWidget,
    updateWidget: suspend (GlanceAppWidget, Context) -> Unit = { w, c -> w.updateAll(c) },
): androidx.work.ListenableWorker.Result = try {
    repository.refreshAll()
    updateWidget(widget, context)
    androidx.work.ListenableWorker.Result.success()
} catch (_: Exception) {
    androidx.work.ListenableWorker.Result.retry()
}
