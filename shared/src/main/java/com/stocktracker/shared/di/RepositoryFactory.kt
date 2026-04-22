package com.stocktracker.shared.di

import android.content.Context
import com.stocktracker.shared.data.api.yahoo.YahooChartApi
import com.stocktracker.shared.data.api.yahoo.YahooFinanceDataSource
import com.stocktracker.shared.data.api.yahoo.YahooSearchApi
import com.stocktracker.shared.data.local.StockDatabase
import com.stocktracker.shared.data.repository.StockRepository
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RepositoryFactory {
    fun create(context: Context): StockRepository {
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
        val searchApi = retrofit.create(YahooSearchApi::class.java)
        val dataSource = YahooFinanceDataSource(api, searchApi)
        val db = StockDatabase.create(context)

        return StockRepository(dataSource, db.stockDao())
    }
}
