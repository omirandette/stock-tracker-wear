package com.stocktracker.data.local

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class StockDaoTest {

    private lateinit var db: StockDatabase
    private lateinit var dao: StockDao

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, StockDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.stockDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun insert_and_getAll() = runTest {
        val entity = StockEntity("AAPL", 150.0, 2.0, "1.35%", 1000L)
        dao.insert(entity)
        val result = dao.getAll().first()
        assertEquals(1, result.size)
        assertEquals("AAPL", result[0].symbol)
        assertEquals(150.0, result[0].price, 0.001)
    }

    @Test
    fun insert_replaces_on_conflict() = runTest {
        dao.insert(StockEntity("AAPL", 150.0, 2.0, "1.35%", 1000L))
        dao.insert(StockEntity("AAPL", 155.0, 7.0, "4.73%", 2000L))
        val result = dao.getAll().first()
        assertEquals(1, result.size)
        assertEquals(155.0, result[0].price, 0.001)
    }

    @Test
    fun delete_removes_stock() = runTest {
        dao.insert(StockEntity("AAPL", 150.0, 2.0, "1.35%", 1000L))
        dao.delete("AAPL")
        assertTrue(dao.getAll().first().isEmpty())
    }

    @Test
    fun getAll_returns_sorted_by_symbol() = runTest {
        dao.insert(StockEntity("GOOG", 2800.0, -15.0, "-0.53%", 1000L))
        dao.insert(StockEntity("AAPL", 150.0, 2.0, "1.35%", 1000L))
        val result = dao.getAll().first()
        assertEquals("AAPL", result[0].symbol)
        assertEquals("GOOG", result[1].symbol)
    }
}
