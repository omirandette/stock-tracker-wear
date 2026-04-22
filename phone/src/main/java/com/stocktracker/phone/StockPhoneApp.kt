package com.stocktracker.phone

import android.app.Application
import com.stocktracker.shared.data.repository.StockRepository
import com.stocktracker.shared.di.RepositoryFactory

open class StockPhoneApp : Application() {

    lateinit var repository: StockRepository
        internal set

    override fun onCreate() {
        super.onCreate()
        repository = RepositoryFactory.create(this)
    }
}
