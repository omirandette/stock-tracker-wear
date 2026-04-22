package com.stocktracker.phone.ui

import com.stocktracker.phone.testutil.MainDispatcherRule
import com.stocktracker.shared.data.repository.StockRepository
import com.stocktracker.shared.model.ChartData
import com.stocktracker.shared.model.ChartPoint
import com.stocktracker.shared.model.TimePeriod
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PhoneStockDetailViewModelTest {

    @get:Rule val dispatcherRule = MainDispatcherRule()

    private val repository = mockk<StockRepository>(relaxed = true)

    @Test
    fun `loadChart exposes data on success`() = runTest {
        val data = ChartData(
            points = listOf(ChartPoint(1L, 100.0), ChartPoint(2L, 110.0)),
            change = 10.0,
            changePercent = 10.0,
        )
        coEvery { repository.getChartData("AAPL", TimePeriod.ONE_DAY) } returns data
        val vm = PhoneStockDetailViewModel(repository)

        vm.loadChart("AAPL", TimePeriod.ONE_DAY)
        advanceUntilIdle()

        assertEquals(data, vm.chartData.value)
        assertFalse(vm.isLoading.value)
        assertNull(vm.error.value)
    }

    @Test
    fun `loadChart sets error on failure`() = runTest {
        coEvery { repository.getChartData("BAD", TimePeriod.ONE_DAY) } throws RuntimeException("fail")
        val vm = PhoneStockDetailViewModel(repository)

        vm.loadChart("BAD", TimePeriod.ONE_DAY)
        advanceUntilIdle()

        assertEquals("Chart load failed", vm.error.value)
        assertFalse(vm.isLoading.value)
    }

    @Test
    fun `loadChart uses cache on second call`() = runTest {
        val data = ChartData(listOf(ChartPoint(1L, 100.0), ChartPoint(2L, 110.0)), 10.0, 10.0)
        coEvery { repository.getChartData("AAPL", TimePeriod.ONE_DAY) } returns data
        val vm = PhoneStockDetailViewModel(repository)

        vm.loadChart("AAPL", TimePeriod.ONE_DAY)
        advanceUntilIdle()
        vm.loadChart("AAPL", TimePeriod.ONE_DAY)
        advanceUntilIdle()

        coVerify(exactly = 1) { repository.getChartData("AAPL", TimePeriod.ONE_DAY) }
    }

    @Test
    fun `loadChart bypasses cache with forceRefresh`() = runTest {
        val data = ChartData(listOf(ChartPoint(1L, 100.0), ChartPoint(2L, 110.0)), 10.0, 10.0)
        coEvery { repository.getChartData("AAPL", TimePeriod.ONE_DAY) } returns data
        val vm = PhoneStockDetailViewModel(repository)

        vm.loadChart("AAPL", TimePeriod.ONE_DAY)
        advanceUntilIdle()
        vm.loadChart("AAPL", TimePeriod.ONE_DAY, forceRefresh = true)
        advanceUntilIdle()

        coVerify(exactly = 2) { repository.getChartData("AAPL", TimePeriod.ONE_DAY) }
    }
}
