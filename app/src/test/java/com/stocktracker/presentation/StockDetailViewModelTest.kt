package com.stocktracker.presentation

import com.stocktracker.data.repository.StockRepository
import com.stocktracker.model.ChartData
import com.stocktracker.model.ChartPoint
import com.stocktracker.model.TimePeriod
import com.stocktracker.testutil.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StockDetailViewModelTest {

    @get:Rule val dispatcherRule = MainDispatcherRule()

    private val repository = mockk<StockRepository>(relaxed = true)
    private var fakeTime = 1_000_000L
    private val vm by lazy { StockDetailViewModel(repository, clock = { fakeTime }) }

    private val points = listOf(ChartPoint(1000L, 150.0), ChartPoint(2000L, 155.0))
    private val chartData = ChartData(points, 2.0, 1.35)

    @Test
    fun `loadChart exposes data on success`() = runTest {
        coEvery { repository.getChartData("AAPL", TimePeriod.ONE_DAY) } returns chartData
        vm.loadChart("AAPL", TimePeriod.ONE_DAY)
        advanceUntilIdle()
        assertEquals(chartData, vm.chartData.value)
        assertFalse(vm.isChartLoading.value)
        assertNull(vm.chartError.value)
    }

    @Test
    fun `loadChart uses cache within TTL`() = runTest {
        coEvery { repository.getChartData("AAPL", TimePeriod.ONE_DAY) } returns chartData
        vm.loadChart("AAPL", TimePeriod.ONE_DAY)
        advanceUntilIdle()
        vm.loadChart("AAPL", TimePeriod.ONE_DAY)
        advanceUntilIdle()
        coVerify(exactly = 1) { repository.getChartData("AAPL", TimePeriod.ONE_DAY) }
    }

    @Test
    fun `loadChart does not share cache across periods`() = runTest {
        coEvery { repository.getChartData("AAPL", any()) } returns chartData
        vm.loadChart("AAPL", TimePeriod.ONE_DAY)
        advanceUntilIdle()
        vm.loadChart("AAPL", TimePeriod.FIVE_DAYS)
        advanceUntilIdle()
        coVerify(exactly = 1) { repository.getChartData("AAPL", TimePeriod.ONE_DAY) }
        coVerify(exactly = 1) { repository.getChartData("AAPL", TimePeriod.FIVE_DAYS) }
    }

    @Test
    fun `loadChart with forceRefresh bypasses cache`() = runTest {
        coEvery { repository.getChartData("AAPL", TimePeriod.ONE_DAY) } returns chartData
        vm.loadChart("AAPL", TimePeriod.ONE_DAY)
        advanceUntilIdle()
        vm.loadChart("AAPL", TimePeriod.ONE_DAY, forceRefresh = true)
        advanceUntilIdle()
        coVerify(exactly = 2) { repository.getChartData("AAPL", TimePeriod.ONE_DAY) }
    }

    @Test
    fun `loadChart re-fetches after TTL expires`() = runTest {
        val staleData = ChartData(points, 2.0, 1.35)
        val freshData = ChartData(points, -3.0, -1.80)
        coEvery { repository.getChartData("AAPL", TimePeriod.ONE_DAY) } returns staleData
        vm.loadChart("AAPL", TimePeriod.ONE_DAY)
        advanceUntilIdle()
        assertEquals(staleData, vm.chartData.value)

        fakeTime += StockDetailViewModel.CACHE_TTL_MS
        coEvery { repository.getChartData("AAPL", TimePeriod.ONE_DAY) } returns freshData
        vm.loadChart("AAPL", TimePeriod.ONE_DAY)
        advanceUntilIdle()
        coVerify(exactly = 2) { repository.getChartData("AAPL", TimePeriod.ONE_DAY) }
        assertEquals(freshData, vm.chartData.value)
    }

    @Test
    fun `loadChart sets error on failure`() = runTest {
        coEvery { repository.getChartData("AAPL", TimePeriod.ONE_DAY) } throws RuntimeException("fail")
        vm.loadChart("AAPL", TimePeriod.ONE_DAY)
        advanceUntilIdle()
        assertEquals("Failed to load chart", vm.chartError.value)
        assertTrue(vm.chartData.value.points.isEmpty())
    }

    @Test
    fun `loadChart clears previous error on success`() = runTest {
        coEvery { repository.getChartData("AAPL", TimePeriod.ONE_DAY) } throws RuntimeException("fail")
        vm.loadChart("AAPL", TimePeriod.ONE_DAY)
        advanceUntilIdle()

        coEvery { repository.getChartData("AAPL", TimePeriod.FIVE_DAYS) } returns chartData
        vm.loadChart("AAPL", TimePeriod.FIVE_DAYS)
        advanceUntilIdle()
        assertNull(vm.chartError.value)
        assertEquals(chartData, vm.chartData.value)
    }

    @Test
    fun `CACHE_TTL_MS is 5 minutes`() {
        assertEquals(300_000L, StockDetailViewModel.CACHE_TTL_MS)
    }
}
