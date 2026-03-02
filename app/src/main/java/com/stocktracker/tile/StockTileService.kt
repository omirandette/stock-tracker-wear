package com.stocktracker.tile

import android.content.ComponentName
import androidx.wear.protolayout.ActionBuilders
import androidx.wear.protolayout.DimensionBuilders.expand
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.LayoutElementBuilders.Column
import androidx.wear.protolayout.ResourceBuilders.Resources
import androidx.wear.protolayout.TimelineBuilders.Timeline
import androidx.wear.protolayout.material3.MaterialScope
import androidx.wear.protolayout.material3.Typography
import androidx.wear.protolayout.material3.materialScope
import androidx.wear.protolayout.material3.primaryLayout
import androidx.wear.protolayout.material3.text
import androidx.wear.protolayout.modifiers.clickable
import androidx.wear.protolayout.types.LayoutString
import androidx.wear.tiles.EventBuilders
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders.Tile
import androidx.wear.tiles.TileService
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.SettableFuture
import com.stocktracker.MainActivity
import com.stocktracker.StockApp
import com.stocktracker.model.Stock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class StockTileService : TileService() {

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onTileRequest(
        requestParams: RequestBuilders.TileRequest,
    ): ListenableFuture<Tile> {
        val future = SettableFuture.create<Tile>()
        val repository = (application as StockApp).repository
        scope.launch {
            try {
                val stocks = repository.watchAll().first()
                val layout = materialScope(
                    context = this@StockTileService,
                    deviceConfiguration = requestParams.deviceConfiguration,
                ) {
                    buildTileLayout(stocks)
                }
                future.set(
                    Tile.Builder()
                        .setResourcesVersion(RESOURCES_VERSION)
                        .setFreshnessIntervalMillis(FRESHNESS_INTERVAL_MS)
                        .setTileTimeline(Timeline.fromLayoutElement(layout))
                        .build()
                )
            } catch (e: Exception) {
                future.setException(e)
            }
        }
        return future
    }

    override fun onTileEnterEvent(requestParams: EventBuilders.TileEnterEvent) {
        getUpdater(this).requestUpdate(StockTileService::class.java)
    }

    override fun onTileResourcesRequest(
        requestParams: RequestBuilders.ResourcesRequest,
    ): ListenableFuture<Resources> = Futures.immediateFuture(
        Resources.Builder().setVersion(RESOURCES_VERSION).build()
    )

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    companion object {
        internal const val RESOURCES_VERSION = "1"
        internal const val FRESHNESS_INTERVAL_MS = 300_000L
        internal const val MAX_STOCKS = 3
    }
}

internal fun MaterialScope.buildTileLayout(
    stocks: List<Stock>,
): LayoutElementBuilders.LayoutElement {
    val launchAction = ActionBuilders.LaunchAction.Builder()
        .setAndroidActivity(
            ActionBuilders.AndroidActivity.Builder()
                .setPackageName("com.stocktracker")
                .setClassName(MainActivity::class.java.name)
                .build()
        )
        .build()

    return primaryLayout(
        onClick = clickable(launchAction, "open_app"),
        titleSlot = {
            text(LayoutString("Stocks"), typography = Typography.TITLE_SMALL)
        },
        mainSlot = {
            if (stocks.isEmpty()) {
                text(LayoutString("No stocks"), typography = Typography.BODY_MEDIUM)
            } else {
                val column = Column.Builder()
                    .setWidth(expand())
                    .setHorizontalAlignment(
                        LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER
                    )
                for (stock in stocks.take(StockTileService.MAX_STOCKS)) {
                    column.addContent(
                        text(
                            LayoutString(formatStockRow(stock)),
                            typography = Typography.BODY_SMALL,
                            maxLines = 1,
                        )
                    )
                }
                column.build()
            }
        },
    )
}

internal fun formatStockRow(stock: Stock): String {
    val price = "%.2f".format(stock.price)
    val sign = if (stock.change >= 0) "+" else ""
    return "${stock.symbol}  $price  $sign${stock.changePercent}"
}
