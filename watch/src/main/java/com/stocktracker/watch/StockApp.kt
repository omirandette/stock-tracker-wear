package com.stocktracker.watch

import android.app.Application
import com.stocktracker.shared.data.repository.StockRepository
import com.stocktracker.shared.di.RepositoryFactory

open class StockApp : Application() {

    lateinit var repository: StockRepository
        internal set

    override fun onCreate() {
        super.onCreate()
        repository = RepositoryFactory.create(this)
    }
}
