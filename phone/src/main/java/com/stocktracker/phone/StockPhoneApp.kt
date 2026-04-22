package com.stocktracker.phone

import android.app.Application
import com.stocktracker.phone.sync.DataLayerWatchlistTransport
import com.stocktracker.phone.sync.WatchlistPublisher
import com.stocktracker.shared.data.repository.StockRepository
import com.stocktracker.shared.di.RepositoryFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

open class StockPhoneApp : Application() {

    lateinit var repository: StockRepository
        internal set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        repository = RepositoryFactory.create(this)
        WatchlistPublisher(
            repository = repository,
            transport = DataLayerWatchlistTransport(this),
            scope = appScope,
        ).start()
    }
}
