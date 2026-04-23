package com.stocktracker.phone.widget

import android.content.Context
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.currentState
import androidx.glance.LocalSize
import com.stocktracker.shared.di.RepositoryFactory

class StockWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Responsive(
        setOf(
            DpSize(180.dp, 110.dp),  // 2x2 compact
            DpSize(320.dp, 220.dp),  // 4x2 medium
            DpSize(480.dp, 440.dp),  // 4x4 large
            DpSize(640.dp, 800.dp),  // full page
        ),
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = RepositoryFactory.create(context.applicationContext)
        provideContent {
            val size = LocalSize.current
            val stocks by repository.watchAll().collectAsState(initial = emptyList())
            StockWidgetContent(
                stocks = stocks,
                size = pickSize(size.width),
            )
        }
    }
}
