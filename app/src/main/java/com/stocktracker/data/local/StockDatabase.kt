package com.stocktracker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [StockEntity::class], version = 1)
abstract class StockDatabase : RoomDatabase() {
    abstract fun stockDao(): StockDao

    companion object {
        fun create(context: Context): StockDatabase =
            Room.databaseBuilder(context, StockDatabase::class.java, "stocks.db").build()
    }
}
