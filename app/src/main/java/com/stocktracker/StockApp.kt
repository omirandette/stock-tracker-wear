package com.stocktracker

import android.app.Application
import com.stocktracker.data.api.StockApiService
import com.stocktracker.data.local.StockDatabase
import com.stocktracker.data.repository.StockRepository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class StockApp : Application() {

    lateinit var repository: StockRepository
        private set

    override fun onCreate() {
        super.onCreate()

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://www.alphavantage.co/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(StockApiService::class.java)
        val db = StockDatabase.create(this)

        repository = StockRepository(api, db.stockDao())
    }
}
