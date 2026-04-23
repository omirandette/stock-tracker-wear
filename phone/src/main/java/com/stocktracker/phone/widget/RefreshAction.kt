package com.stocktracker.phone.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.action.ActionCallback
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

class RefreshAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: androidx.glance.action.ActionParameters,
    ) {
        val request = OneTimeWorkRequestBuilder<WatchlistRefreshWorker>().build()
        WorkManager.getInstance(context.applicationContext).enqueue(request)
    }
}
