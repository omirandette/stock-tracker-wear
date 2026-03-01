package com.stocktracker

import android.app.Application
import com.stocktracker.data.api.yahoo.YahooChartApi
import com.stocktracker.data.api.yahoo.YahooFinanceDataSource
import com.stocktracker.data.local.StockDatabase
import com.stocktracker.data.repository.StockRepository
import okhttp3.Interceptor
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
        val userAgent = Interceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", "Mozilla/5.0")
                .build()
            chain.proceed(request)
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(userAgent)
            .addInterceptor(logging)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://query1.finance.yahoo.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(YahooChartApi::class.java)
        val dataSource = YahooFinanceDataSource(api)
        val db = StockDatabase.create(this)

        repository = StockRepository(dataSource, db.stockDao())
    }
}
